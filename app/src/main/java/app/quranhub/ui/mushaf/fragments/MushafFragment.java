package app.quranhub.ui.mushaf.fragments;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.entity.Aya;
import app.quranhub.data.local.entity.TranslationBook;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.data.model.ReciterModel;
import app.quranhub.data.service.QuranAudioDownloaderService;
import app.quranhub.databinding.FragmentMushafBinding;
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment;
import app.quranhub.ui.main.MainActivity;
import app.quranhub.ui.mushaf.adapter.QuranViewPagerAdapter;
import app.quranhub.ui.mushaf.audio_manager.AudioStateEvent;
import app.quranhub.ui.mushaf.audio_manager.AyaAudioService;
import app.quranhub.ui.mushaf.audio_manager.SharedRepeatModel;
import app.quranhub.ui.mushaf.dialogs.AyaAudioPopup;
import app.quranhub.ui.mushaf.dialogs.AyaRecorderDialog;
import app.quranhub.ui.mushaf.dialogs.AyaRecorderPlayerDialog;
import app.quranhub.ui.mushaf.dialogs.AyaRepeatDialog;
import app.quranhub.ui.mushaf.dialogs.TranslationsDialogFragment;
import app.quranhub.ui.mushaf.events.QuranPageClickEvent;
import app.quranhub.ui.mushaf.model.QuranPageInfo;
import app.quranhub.ui.mushaf.model.RepeatModel;
import app.quranhub.ui.mushaf.model.SuraVersesNumber;
import app.quranhub.ui.mushaf.presenter.Mus7fPresenter;
import app.quranhub.ui.mushaf.presenter.Mus7fPresenterImp;
import app.quranhub.ui.mushaf.view.MushafView;
import app.quranhub.util.LocaleUtils;
import app.quranhub.util.ScreenUtils;
import app.quranhub.util.SharedPrefsUtils;
import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;


public class MushafFragment extends Fragment implements MushafView, MushafBottomBarFragment.QuranFooterCallbacks
        , TranslationsDataFragment.TranslationSelectionListener, AyaAudioPopup.AyaAudioListener,
        AyaRecorderDialog.StopRecordingListener, QuranRecitersDialogFragment.ReciterSelectionListener,
        AyaRecorderPlayerDialog.AyaRecorderPlayerListener, AyaRepeatDialog.AyaRepeatListener {

    private static final String TAG = MushafFragment.class.getSimpleName();

    private static final String ARG_INIT_PAGE = "ARG_INIT_PAGE";
    private static final String ARG_FROM_NOTFICATION = "ARG_FROM_NOTIFICATION";
    private static final String ARG_INIT_AYA = "ARG_INIT_AYA";
    private static final int REQUEST_RECORDING_PERM = 1;

    private FragmentMushafBinding binding;

    private int quranPageIndex;
    private int initAyaId, ayaNumber;
    private int suraNumber, ayaId;
    private String currentSuraName;
    private TranslationBook book;
    private String currentTafsserId;
    private String currentTafseerLang;
    private Handler seekbarPageHanlder;
    private Runnable seekbarPageRunnable;
    private ArrayList<ArrayList<Integer>> pageSuras;    // 2D list include suras numbers in each page
    private String[] surasName;
    private AyaAudioPopup ayaAudioPopup;
    private QuranPageFragment quranPageFragment;
    private boolean ayaHasRecorder = false;
    private ArrayList<SuraVersesNumber> suraVersesNumberArrayList;
    private Aya selectedAyaAudio;
    private int recitationId;
    private boolean isOriented = false;

    private String bookDbName = "default", bookName;
    private BottomSheetBehavior sheetBehavior;
    private QuranViewPagerAdapter pagerAdapter;
    private MushafBottomBarFragment footerbarFragment;
    private MushafTopBarFragment headerbarFragment;
    private Mus7fPresenter presenter;
    private boolean isBottomSheetVisible = false, isAudioDialogOpen = false, isAudioPlay = false;
    private boolean initAudioOnFirstAya = false, initAudioInRepeatGroup = false, initAyaFromNotifcation = false;

    private int startGroupRepeatPage, firstAyaInRepeatGroup, fromSuraDownloaded;
    private Aya notficationCurrentAya;
    private Intent audioServiceIntent;

    public static MushafFragment newInstance() {
        return new MushafFragment();
    }

    /**
     * Create a MushafFragment instance initialized at the given page.
     *
     * @param initPageNumber
     * @return
     */
    public static MushafFragment newInstance(int initPageNumber) {
        MushafFragment mushafFragment = new MushafFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_INIT_PAGE, initPageNumber);
        mushafFragment.setArguments(bundle);
        return mushafFragment;
    }

    /**
     * Create a MushafFragment instance initialized at the given page & highlighting the given aya.
     *
     * @param initPageNumber
     * @param initAyaId
     * @return
     */
    public static MushafFragment newInstance(int initPageNumber, int initAyaId) {
        MushafFragment mushafFragment = new MushafFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_INIT_PAGE, initPageNumber);
        bundle.putInt(ARG_INIT_AYA, initAyaId);
        mushafFragment.setArguments(bundle);
        return mushafFragment;
    }

    public static MushafFragment newNotificationInstance(int ayaId) {
        MushafFragment mushafFragment = new MushafFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_FROM_NOTFICATION, true);
        bundle.putInt(ARG_INIT_AYA, ayaId);
        mushafFragment.setArguments(bundle);
        return mushafFragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("auido_dialog_state", isAudioDialogOpen);
        outState.putBoolean("auido_playing_state", isAudioPlay);
        outState.putBoolean("aya_has_recorder", ayaHasRecorder);
        outState.putInt("aya_id", ayaId);
        outState.putInt("start_group_page", startGroupRepeatPage);
        outState.putInt("first_repeat_aya", firstAyaInRepeatGroup);
        outState.putInt("from_sura_downloaded", fromSuraDownloaded);
        outState.putInt("page_index", quranPageIndex);
        if (sheetBehavior != null && sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            onCloseTranslationDiaog();
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMushafBinding.inflate(inflater, container, false);
        initPresenter();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attachListeners();
    }

    private void observeOnBottomSheetChanged() {
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    isBottomSheetVisible = false;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }

    private void initPresenter() {
        presenter = new Mus7fPresenterImp(requireActivity());
        presenter.onAttach(this);
        presenter.getSurasInPage();
        presenter.getSuraNumofVerses();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        quranPageIndex = Constants.Quran.NUM_OF_PAGES - 1;
        initAyaId = -1; // No aya selected
        initFragments();
        getFragmentArguments(savedInstanceState != null);
        getPrevState(savedInstanceState);
        listenViewPagerSwipe();
        initViewPager(savedInstanceState != null);
        observeOnBottomSheetChanged();

        observeOnQuranSeekbarChange();

        if (AppPreferencesManager.getScreenReadingBacklightSetting(requireContext())) {
            // disable the screen timeout
            ScreenUtils.keepScreenOn(requireActivity(), true);
        }

        checkOrientationType();
    }

    private void getPrevState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isOriented = true;
            isAudioPlay = savedInstanceState.getBoolean("auido_playing_state");
            isAudioDialogOpen = savedInstanceState.getBoolean("auido_dialog_state");
            ayaHasRecorder = savedInstanceState.getBoolean("aya_has_recorder");
            firstAyaInRepeatGroup = savedInstanceState.getInt("first_repeat_aya");
            fromSuraDownloaded = savedInstanceState.getInt("from_sura_downloaded");
            ayaId = savedInstanceState.getInt("aya_id");
            quranPageIndex = savedInstanceState.getInt("page_index");
            startGroupRepeatPage = savedInstanceState.getInt("start_group_page");
            checkAudioDialogState();
        }
    }

    private void getFragmentArguments(boolean withPrevState) {
        surasName = getResources().getStringArray(R.array.sura_name);
        if (getArguments() != null) {
            int selectedPage = getArguments().getInt(ARG_INIT_PAGE, 1);
            quranPageIndex = Constants.Quran.NUM_OF_PAGES - selectedPage;
            initAyaId = getArguments().getInt(ARG_INIT_AYA, -1);
            if (getArguments().getBoolean(ARG_FROM_NOTFICATION, false) && initAyaId != -1 && !withPrevState) {
                selectNotificationAya();
            }
            SharedPrefsUtils.saveInteger(requireActivity(), "last_open_page", quranPageIndex);
        }
    }

    private void selectNotificationAya() {
        presenter.getNotificationAya(initAyaId);
        openAyaAudioDialog();
        checkAyaRecorderState(initAyaId);
        if (SharedPrefsUtils.getBoolean(requireActivity(), AyaAudioService.AUDIO_PLAYING, false)) {
            ayaAudioPopup.setPlayState();
        }
    }

    private void checkAudioDialogState() {
        if (isAudioDialogOpen) {
            binding.quranViewpager.post(() -> {
                ayaAudioPopup.showPopup(binding.quranViewpager);
            });
            if (isAudioPlay) {
                ayaAudioPopup.setPlayState();
            }
            ayaAudioPopup.setRecordState(ayaHasRecorder);
        }
    }


    // hide quran seekbar in landscape mode
    private void checkOrientationType() {
        if (ScreenUtils.isLandscape(getActivity())) {
            binding.quranSeekbar.setVisibility(View.GONE);
        }
    }

    // observe on seek bar changes from user to show its progress for page number and sura name
    private void observeOnQuranSeekbarChange() {

        seekbarPageHanlder = new Handler();
        seekbarPageRunnable = () -> binding.pageSeekTv.setVisibility(View.GONE);

        binding.quranSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekbarPageHanlder.removeCallbacks(seekbarPageRunnable);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setSeekPageInfo(progress);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.quranViewpager.setCurrentItem(Constants.Quran.NUM_OF_PAGES - seekBar.getProgress());
                seekbarPageHanlder.postDelayed(seekbarPageRunnable, 1000);
            }
        });

    }

    // set page and its suras when change progress of fast Quran seekbar
    private void setSeekPageInfo(int progress) {
        if (progress == 0)
            progress = 1;
        String surahsInPage = "", progressStr = LocaleUtils.formatNumber(progress);
        binding.pageSeekTv.setVisibility(View.VISIBLE);
        if (pageSuras != null) {
            int numOfSurasInPage = pageSuras.get(progress).size();
            for (int i = 0; i < numOfSurasInPage; i++) {
                surahsInPage += surasName[pageSuras.get(progress).get(i) - 1];
                if (i != numOfSurasInPage - 1) {
                    surahsInPage += " , ";
                }
            }
        }
        binding.pageSeekTv.setText(surahsInPage + "\n" + progressStr);
    }

    private void setupMus7afShowcase() {
        if (ScreenUtils.isPortrait(requireActivity())) { // FancyShowCaseView works very bad on landscape
            // Showcase to notify the user about the clicking action on the sura name in `toolbarFragment`
            FancyShowCaseView suraNameShowCaseView = new FancyShowCaseView.Builder(requireActivity())
                    .focusOn(headerbarFragment.requireView())
                    .title(getString(R.string.showcase_title_sura_index))
                    .enableAutoTextPosition()
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .roundRectRadius(90)
                    .fitSystemWindows(true)
                    .delay(300)
                    .showOnce("sura_name_showcase")
                    .build();

            // Showcase to notify the user about long clicking ayas in screen
            FancyShowCaseView ayaShowCaseView = new FancyShowCaseView.Builder(requireActivity())
                    .focusOn(binding.quranViewpager)
                    .title(getString(R.string.showcase_title_aya_longclick))
                    .focusCircleRadiusFactor(0.4f)
                    .fitSystemWindows(true)
                    .showOnce("aya_longclick_showcase")
                    .build();

            new FancyShowCaseQueue()
                    .add(suraNameShowCaseView)
                    .add(ayaShowCaseView)
                    .show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        registerEventBus();

        setupMus7afShowcase();
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPrefsUtils.saveInteger(requireActivity(), "last_open_page", quranPageIndex);
    }

    @Override
    public void onStop() {
        super.onStop();
        unRegisterEventBus();
    }

    @Override
    public void onDestroyView() {
        presenter.onDetach();
        super.onDestroyView();

        binding = null;

        if (AppPreferencesManager.getScreenReadingBacklightSetting(requireContext())) {
            // re-enable the screen timeout
            ScreenUtils.keepScreenOn(requireActivity(), false);
        }
        dismissAudioPopup();
    }

    private void listenViewPagerSwipe() {
        binding.quranViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setSelectedPageViews(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setSelectedPageViews(int position) {
        quranPageFragment = null;
        quranPageIndex = position;
        setPageNumber(position);
        setSeekbarProgress(position);
        setPageDir();
        presenter.getQuranPageInfo(position);
        onCloseTranslationDiaog();
        if (!isOriented) {
            checkAudioAutoPlay();
        } else {
            isOriented = false;
        }
    }

    private void checkAudioAutoPlay() {

        setCurrentQuranPageFragment();
        // auto play audio when user swip to page and the audio is playing OR last aya in page is finished and will start audio for first aya in new paeg

        if (initAudioOnFirstAya || (isAudioDialogOpen && isAudioPlay)) {
            quranPageFragment.playFirstAyaAudio();
            initAudioOnFirstAya = false;
        }

        // play audio for from-aya in repeating interval
        if (initAudioInRepeatGroup) {
            initAudioInRepeatGroup = false;
            quranPageFragment.playMiddleAyaAudio();
        }

        // set current aya with draw shadown when open mushaf from audio notification
        if (initAyaFromNotifcation) {
            initAyaFromNotifcation = false;
            if (SharedPrefsUtils.getBoolean(requireActivity(), AyaAudioService.AUDIO_PLAYING, false)) {
                isAudioPlay = true;
            }
            quranPageFragment.setCurrentAyaFromNotification(notficationCurrentAya);
        }
    }


    private void setCurrentQuranPageFragment() {
        if (quranPageFragment == null) {
            quranPageFragment = (QuranPageFragment) Objects.requireNonNull(binding.quranViewpager
                            .getAdapter())
                    .instantiateItem(binding.quranViewpager, binding.quranViewpager.getCurrentItem());
        }
    }

    private void setPageDir() {
        if (quranPageIndex % 2 == 0) { // quranPageIndex is even
            headerbarFragment.setPageDir(MushafTopBarFragment.PAGE_DIR_LEFT);
        } else { // quranPageIndex is odd
            headerbarFragment.setPageDir(MushafTopBarFragment.PAGE_DIR_RIGHT);
        }
    }

    private void setPageNumber(int position) {
        String page = LocaleUtils.formatNumber(Constants.Quran.NUM_OF_PAGES - position);
        footerbarFragment.setCurrentPage(page);
    }

    private void setSeekbarProgress(int position) {
        binding.quranSeekbar.setProgress(Constants.Quran.NUM_OF_PAGES - position);
    }

    private void initFragments() {
        currentTafsserId = AppPreferencesManager.getQuranTranslationBook(requireActivity());
        currentTafseerLang = AppPreferencesManager.getQuranTranslationLanguage(requireActivity());
        recitationId = AppPreferencesManager.getRecitationSetting(requireContext());
        ayaAudioPopup = new AyaAudioPopup(requireActivity(), this);
        bookName = getString(R.string.translation_muyassar);
        binding.translationBottomSheet.translationSheetTv.setMovementMethod(new ScrollingMovementMethod());
        sheetBehavior = BottomSheetBehavior.from(binding.translationBottomSheet.bottomSheet);
        FragmentManager fragmentManager = getChildFragmentManager();
        footerbarFragment = (MushafBottomBarFragment) fragmentManager.findFragmentById(R.id.footerbar_fragment);
        headerbarFragment = (MushafTopBarFragment) fragmentManager.findFragmentById(R.id.top_bar_fragment);
        presenter.getQuranPageInfo(quranPageIndex);
    }

    private void initViewPager(boolean isInstanceSaved) {
        String quranImageBaseUrl;
        List<String> quranPageImages = new ArrayList<>();
        switch (recitationId) {
            case Constants.Recitation.HAFS_ID:
                quranImageBaseUrl = Constants.Quran.HAFS_IMG_BASE_URL;
                for (int i = Constants.Quran.NUM_OF_PAGES; i >= 1; i--) {
                    String imageName = String.format(Locale.US, "%03d.jpg", i);
                    quranPageImages.add(quranImageBaseUrl + imageName);
                }
                break;
            case Constants.Recitation.WARSH_ID:
                quranImageBaseUrl = Constants.Quran.WARSH_IMG_BASE_URL;
                for (int i = Constants.Quran.NUM_OF_PAGES; i >= 1; i--) {
                    String imageName = String.format(Locale.US, "%d.png", i);
                    quranPageImages.add(quranImageBaseUrl + imageName);
                }
                break;
            default:
                throw new RuntimeException("Cannot identify recitation");
        }
        pagerAdapter = new QuranViewPagerAdapter(getChildFragmentManager()
                , quranPageImages, presenter.getNightMode(), initAyaId);
        binding.quranViewpager.setAdapter(pagerAdapter);
        if (!isInstanceSaved) {
            binding.quranViewpager.setCurrentItem(quranPageIndex);
        }
        setSeekbarProgress(quranPageIndex);
        // handle bug in viewpager onSelectedPage callback not called when position is 0
        if (quranPageIndex == 0) {
            setSelectedPageViews(0);
        }
    }

    @Override
    public void showQuranPageInfo(QuranPageInfo quranPageInfo) {
        String suraName = getResources().getStringArray(R.array.sura_name)[quranPageInfo.getSura() - 1];
        String guz2Name = getResources().getStringArray(R.array.agza2_name)[quranPageInfo.getJuz() - 1];
        headerbarFragment.setSuraText(suraName);
        headerbarFragment.setGuz2Text(guz2Name);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public void dismissAudioPopup() {
        if (ayaAudioPopup != null && isAudioDialogOpen) {
            ayaAudioPopup.dismissPopup();
            isAudioDialogOpen = false;
        }
    }

    @Override
    public void showLoading() {
    }

    @Override
    public void hideLoading() {
    }


    private void toggleQuranBars() {
        if (binding.barsGroup.getVisibility() == View.VISIBLE) {
            binding.barsGroup.setVisibility(View.GONE);
        } else {
            binding.barsGroup.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onQuranPageClick(QuranPageClickEvent event) {
        toggleQuranBars();
    }


    @Override
    public void openSearchFragment() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.openSearchFragment();
        }
    }

    @Override
    public boolean toggleNightMode() {
        boolean newNightMode = presenter.toggleNightMode();
        pagerAdapter.setNightMode(newNightMode);  // update current session
        return newNightMode;
    }

    private void attachListeners() {
        binding.translationBottomSheet.closeBtn.setOnClickListener(v -> onCloseTranslationDiaog());
        binding.translationBottomSheet.tvBookName.setOnClickListener(v -> onBookNameClicked());
        binding.translationBottomSheet.moreBtn.setOnClickListener(v -> onClickMore());
        binding.translationBottomSheet.nextIv.setOnClickListener(v -> onGetNextAyaTafseer());
        binding.translationBottomSheet.prevIv.setOnClickListener(v -> onClickPrevAya());
    }

    private void onCloseTranslationDiaog() {
        if (sheetBehavior.getPeekHeight() > 0) {
            sheetBehavior.setPeekHeight(0);
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            isBottomSheetVisible = false;
        }
    }

    public void openTranslationDialog(Aya selectedAya) {
        dismissAudioPopup();
        binding.translationBottomSheet.sheetProgreesBar.setVisibility(View.VISIBLE);
        // show default tafseer from locale db when tafseer lang is arabic
        if (currentTafsserId == null && currentTafseerLang.equals(Constants.Language.ARABIC_CODE)) {         // get aya tafseer from default book ("EL-Meyser")
            onGetAyaTafseer(selectedAya.getTafseer());
            binding.translationBottomSheet.nextIv.setVisibility(View.VISIBLE);
            binding.translationBottomSheet.prevIv.setVisibility(View.VISIBLE);
        } else if (book == null) {
            // load translation book from TranslationDB if exist to get aya translation from it
            presenter.getCurrentTafseerBook(currentTafsserId);
        } else {
            presenter.getAyaTafseer(selectedAya.getId());
        }
        ayaId = selectedAya.getId();
        ayaNumber = selectedAya.getSuraAya();
        this.suraNumber = selectedAya.getSura();
        currentSuraName = getResources().getStringArray(R.array.sura_name)[selectedAya.getSura() - 1];
    }

    @Override
    public void onGetAyaTafseer(String tafseer) {
        binding.translationBottomSheet.sheetProgreesBar.setVisibility(View.GONE);
        binding.translationBottomSheet.translationSheetTv.scrollTo(0, 0);
        if (!isBottomSheetVisible) {
            sheetBehavior.setPeekHeight((int) getResources().getDimension(com.intuit.sdp.R.dimen._105sdp));
        }
        binding.translationBottomSheet.translationSheetTv.setText(tafseer);
    }

    // load translation book successfully from TranslationDB
    @Override
    public void onGetTafseerBook(TranslationBook book) {
        bookDbName = book.getDatabaseName();
        binding.translationBottomSheet.tvBookName.setText(book.getName());
        presenter.getAyaTafseer(ayaId);
        this.book = book;
        bookName = book.getName();
        binding.translationBottomSheet.nextIv.setVisibility(View.VISIBLE);
        binding.translationBottomSheet.prevIv.setVisibility(View.VISIBLE);
    }

    // selected translation book is not exist in TranslationDB
    @Override
    public void onNoBooksExist() {
        onGetAyaTafseer(getString(R.string.no_downloaded_books));
        binding.translationBottomSheet.tvBookName.setText(getString(R.string.choose_book));
    }

    // get Suras for each page in quran to show when user use bottom seekbar
    @Override
    public void onGetPageSuras(ArrayList<ArrayList<Integer>> suras) {
        this.pageSuras = suras;
    }

    // when selected aya has recorder
    @Override
    public void onGetAyaRecorder(String recorderPath) {
        if (recorderPath != null && ayaAudioPopup != null) {
            ayaAudioPopup.setRecordState(true);
            ayaHasRecorder = true;
        }
    }

    @Override
    public void onGetSuraVersesNumber(ArrayList<SuraVersesNumber> suraVersesNumbers) {
        suraVersesNumberArrayList = suraVersesNumbers;
        setAudioService();
    }

    // show list of available books for translation language
    private void onBookNameClicked() {
        String transLang = AppPreferencesManager.getQuranTranslationLanguage(requireContext());
        TranslationsDialogFragment translationsDialog = TranslationsDialogFragment.newInstance(
                transLang, this);
        translationsDialog.show(getParentFragmentManager(), "trans_dialog");
    }

    // open tafseer screen to show its Ayas with thier translation
    private void onClickMore() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.openTafseerScreen(currentSuraName, suraNumber, bookDbName, bookName, ayaNumber);
        }
    }

    // User choose book from available downloaded translation books
    @Override
    public void onTranslationSelected(TranslationBook translationBook) {
        presenter.onGetTafsserBook(translationBook);
        currentTafsserId = translationBook.getId();
    }

    private void setTafseerUserAction(boolean isClickPrev) {
        quranPageFragment.drawActionShadow(isClickPrev);
        Aya selectedAya = quranPageFragment.getCurrentAya();
        if (book != null) {
            presenter.getAyaTafseer(selectedAya.getId());
        } else {
            onGetAyaTafseer(selectedAya.getTafseer());
        }
    }

    // get next aya tafseer
    private void onGetNextAyaTafseer() {
        setCurrentQuranPageFragment();
        int selectedAyaIndex = quranPageFragment.getCurrentAyaIndex();
        int numOfAyaInPage = quranPageFragment.getNumOfAyaInPage();
        if (selectedAyaIndex != numOfAyaInPage - 1) {
            setTafseerUserAction(false);
        }
    }

    // get prev aya tafseer
    private void onClickPrevAya() {
        setCurrentQuranPageFragment();
        int selectedAyaIndex = quranPageFragment.getCurrentAyaIndex();
        if (selectedAyaIndex != 0) {
            setTafseerUserAction(true);
        }
    }

    // open audio dailog actions for selected aya
    public void openAyaAudioDialog() {
        onCloseTranslationDiaog();
        binding.barsGroup.setVisibility(View.GONE);
        if (!isAudioDialogOpen) {
            binding.quranViewpager.post(() -> {
                ayaAudioPopup.showPopup(binding.quranViewpager);
            });
            isAudioDialogOpen = true;
        }
    }

    // check if selected aya has user recorder voice
    public void checkAyaRecorderState(int ayaId) {
        this.ayaId = ayaId;
        presenter.checkAyaHasRecorder(ayaId);
        ayaHasRecorder = false;
        ayaAudioPopup.setRecordState(false);
    }


    @Override
    public void checkPlayPauseState() {
        setCurrentQuranPageFragment();
        // if play audio with no selected aya => the audio play on first aya in page

        if (isAudioPlay) {
            togglePauseState(true);
        } else {
            if (quranPageFragment.getCurrentAya() == null)
                quranPageFragment.playFirstAyaAudio();
            else
                togglePlayState(true);
        }


    }

    @Override
    public void onClickRepeat() {
        setCurrentQuranPageFragment();
        Aya currentAya = quranPageFragment.getCurrentAya();
        AyaRepeatDialog repeatDialog = AyaRepeatDialog.getInstance(suraVersesNumberArrayList, currentAya);
        repeatDialog.show(getChildFragmentManager(), "AyaRepeatDialog");
    }

    public void playNextAyaAudio() {
        if (quranPageFragment.getCurrentAya() != null) {
            if (quranPageFragment.getCurrentAyaIndex() != quranPageFragment.getNumOfAyaInPage() - 1) {
                audioServiceIntent.setAction(AyaAudioService.ACTION_NEXT);
                audioServiceIntent.putExtra(AyaAudioService.AYA_ID_KEY, quranPageFragment.getCurrentAyaId() + 1);
                requireActivity().startService(audioServiceIntent);
            } else {
                swipToNextQuranPage();
            }
        }
    }

    public void playPrevAyaAudio() {

        if (quranPageFragment.getCurrentAya() != null) {
            if (quranPageFragment.getCurrentAyaIndex() != 0) {
                audioServiceIntent.setAction(AyaAudioService.ACTION_PREVIOUS);
                audioServiceIntent.putExtra(AyaAudioService.AYA_ID_KEY, quranPageFragment.getCurrentAyaId() - 1);
                requireActivity().startService(audioServiceIntent);
            } else {
                swipToPrevQuranPage();
            }
        }
    }

    // handle user click to play next aya audio
    @Override
    public void onPlayNextAya() {
        setCurrentQuranPageFragment();
        playNextAyaAudio();
    }

    // handle user click to play prev aya audio
    @Override
    public void onPlayPrevAya() {
        setCurrentQuranPageFragment();
        playPrevAyaAudio();
    }


    @Override
    public void onPressRecord() {
        if (isAudioPlay) {
            togglePauseState(true);
        }
        ayaAudioPopup.dismissPopup();
        // open aya recorder player if selected aya has recorder voice
        if (ayaHasRecorder) {
            openAyaRecorderPlayer();
        } else {
            // get permission to access microphone and mobile storage to save user voice recorder on phone
            getRecordingPerm();
        }
    }

    @Override
    public void onClickReciter() {
        QuranRecitersDialogFragment recitersDialogFragment = QuranRecitersDialogFragment
                .newInstance(recitationId);
        recitersDialogFragment.show(getChildFragmentManager(), "QuranRecitersDialogFragment");
    }

    @Override
    public void onClickStop() {
        setCurrentQuranPageFragment();
        isAudioPlay = false;
        ayaHasRecorder = false;
        isAudioDialogOpen = false;
        ayaAudioPopup.dismissPopup();
        requireActivity().stopService(audioServiceIntent);
    }


    private void openAyaRecorderPlayer() {
        isAudioDialogOpen = false;
        AyaRecorderPlayerDialog dialog = AyaRecorderPlayerDialog.getInstance(ayaId);
        dialog.show(getChildFragmentManager(), "AyaRecorderPlayerDialog");
    }

    private void getRecordingPerm() {
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireActivity(), permissions[0]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(requireActivity(), permissions[1]) == PackageManager.PERMISSION_GRANTED) {
                startAyaRecording();
            } else {
                requestPermissions(permissions, REQUEST_RECORDING_PERM);
            }
        } else {
            startAyaRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = true;
        if (requestCode == REQUEST_RECORDING_PERM) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }
            if (isGranted) {
                startAyaRecording();
            } else {
                Toast.makeText(getActivity(), getString(R.string.accept_perm), Toast.LENGTH_LONG).show();
            }
        }
    }

    // start voice recording of selected aya
    private void startAyaRecording() {
        isAudioDialogOpen = false;
        AyaRecorderDialog recorderDialog = AyaRecorderDialog.getInstance(ayaId);
        recorderDialog.show(getChildFragmentManager(), "AyaRecorderDialog");
    }

    @Override
    public void onStopRecording(String recorderPath) {
        presenter.saveRecorderPath(ayaId, recorderPath);
        ayaAudioPopup.showPopup(binding.quranViewpager);
        ayaAudioPopup.setRecordState(true);
        ayaHasRecorder = true;
        isAudioDialogOpen = true;
    }

    @Override
    public void onClickDeleteRecorder() {
        ayaAudioPopup.showPopup(binding.quranViewpager);
        isAudioDialogOpen = true;
        ayaHasRecorder = false;
        ayaAudioPopup.setRecordState(false);
        presenter.deleteAyaVoiceRecorder(ayaId);
    }


    private void autoSwipPage(int page) {
        isOriented = false;
        isAudioPlay = false;
        ayaAudioPopup.setPauseState();
        binding.quranViewpager.setCurrentItem(page, true);
    }

    // start audio of selected aya after it downloaded its sura audios
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onDownloadAudioFinished(QuranAudioDownloaderService.DownloadFinishEvent event) {
        if (!isAudioDialogOpen)
            return;
        firstAyaInRepeatGroup = selectedAyaAudio.getSuraAya();
        fromSuraDownloaded = selectedAyaAudio.getSura();
        Log.d("ww9", "onDownloadAudioFinished: " + firstAyaInRepeatGroup + " , " + fromSuraDownloaded);
        if (selectedAyaAudio != null && quranPageIndex != Constants.Quran.NUM_OF_PAGES - selectedAyaAudio.getPage()) {
            initAudioInRepeatGroup = true;
            autoSwipPage(Constants.Quran.NUM_OF_PAGES - selectedAyaAudio.getPage());
        } else {
            setCurrentQuranPageFragment();
            quranPageFragment.playMiddleAyaAudio();
        }
    }

    // go to next quran page after last aya audio was finished in current page
    public void swipToNextQuranPage() {
        if (quranPageIndex != 0) {
            initAudioOnFirstAya = true;
            autoSwipPage(quranPageIndex - 1);
        }
    }

    public void swipToPrevQuranPage() {
        if (quranPageIndex != Constants.Quran.NUM_OF_PAGES) {
            initAudioOnFirstAya = true;
            autoSwipPage(quranPageIndex + 1);
        }
    }

    // swip to from-aya interval when repeating group is finsihed
    public void swipToFirstAyaInRepeatGroup() {
        setCurrentQuranPageFragment();
        if (startGroupRepeatPage != quranPageIndex) {
            initAudioInRepeatGroup = true;
            autoSwipPage(startGroupRepeatPage);
        } else {
            quranPageFragment.playMiddleAyaAudio();
        }
    }


    public int getFirstAyaInRepeatGroup() {
        return firstAyaInRepeatGroup;
    }

    public int getFromSuraDownloaded() {
        return fromSuraDownloaded;
    }


    /**
     * when select start repeat
     * set first aya of repeating to shadow it and play its audio after group is finished (if repeating group > 1)
     * get first aya of repeating page to swip to it and start play audio
     *
     * @param repeatModel
     */
    @Override
    public void onAyasRepeat(RepeatModel repeatModel) {
        SharedRepeatModel.setRepeatModel(repeatModel);
        SharedRepeatModel.isRepeatModelChanged = true;
        firstAyaInRepeatGroup = repeatModel.getFromAya();
        fromSuraDownloaded = repeatModel.getFromSura();
        presenter.getFromAyaPage(repeatModel.getFromAyaId());
    }

    // swipe to page which contain from-aya interval in repeating
    @Override
    public void onGetAyaPage(int page) {
        setCurrentQuranPageFragment();
        page = Constants.Quran.NUM_OF_PAGES - page;
        startGroupRepeatPage = page;
        if (page != quranPageIndex) {
            initAudioInRepeatGroup = true;
            autoSwipPage(page);
        } else {
            quranPageFragment.playMiddleAyaAudio();
        }
    }

    public void setSelectedAyaAudio(Aya selectedAyaAudio) {
        this.selectedAyaAudio = selectedAyaAudio;
    }

    @Override
    public void onReciterSelected(int recitationId, @NonNull ReciterModel reciter) {
        setCurrentQuranPageFragment();
        quranPageFragment.onListenClick();
    }

    private void setAudioService() {
        if (audioServiceIntent == null) {
            audioServiceIntent = new Intent(getActivity(), AyaAudioService.class);
            audioServiceIntent.putExtra(AyaAudioService.SURA_VERSES_KEY, suraVersesNumberArrayList);
        }
    }

    // play audio for selected aya
    public void resumeAyaAudio() {
        audioServiceIntent.setAction(AyaAudioService.ACTION_RESUME);
        requireActivity().startService(audioServiceIntent);
    }

    // pause audio of selected aya
    public void pauseAyaAudio() {
        audioServiceIntent.setAction(AyaAudioService.ACTION_PAUSE);
        requireActivity().startService(audioServiceIntent);
    }

    public void playAudioService() {
        setCurrentQuranPageFragment();
        if (quranPageFragment.getCurrentAya() != null) {
            audioServiceIntent.putExtra(AyaAudioService.AYA_ID_KEY, quranPageFragment.getCurrentAyaId());
            audioServiceIntent.setAction(AyaAudioService.ACTION_PLAY);
            requireActivity().startService(audioServiceIntent);
        }
    }

    public void togglePlayState(boolean checkPlayAudio) {
        setCurrentQuranPageFragment();
        isAudioPlay = true;
        ayaAudioPopup.setPlayState();
        if (checkPlayAudio) {
            if (quranPageFragment.isAyaAudioDownloaded())
                resumeAyaAudio();
            else
                playAudioService();
        }
    }

    public void togglePauseState(boolean checkPauseAudio) {
        isAudioPlay = false;
        ayaAudioPopup.setPauseState();
        if (checkPauseAudio)
            pauseAyaAudio();
    }

    // get current aya info which it playing in audio notification
    @Override
    public void onGetCurrentAyaFromNotification(Aya aya) {
        initAyaFromNotifcation = true;
        notficationCurrentAya = aya;
        binding.quranViewpager.setCurrentItem(Constants.Quran.NUM_OF_PAGES - aya.getPage());
    }

    private void registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    private void unRegisterEventBus() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }


    // Eventbus subscriber to recieve audio states from audio foreground service
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onAudioStateChanged(AudioStateEvent audioStateEvent) {
        setCurrentQuranPageFragment();
        if (audioStateEvent.getAudioState() == AudioStateEvent.State.PAUSED) {
            togglePauseState(false);
        } else if (audioStateEvent.getAudioState() == AudioStateEvent.State.RESUME || audioStateEvent.getAudioState() == AudioStateEvent.State.PLAYING) {
            quranPageFragment.setAyaAudioDownloaded(true);
            togglePlayState(false);
        } else if (audioStateEvent.getAudioState() == AudioStateEvent.State.PLAY_NEXT) {
            if (quranPageFragment.getCurrentAyaIndex() != quranPageFragment.getNumOfAyaInPage() - 1) {
                quranPageFragment.drawActionShadow(false);
                checkAyaRecorderState(quranPageFragment.getCurrentAyaId());
            } else {
                swipToNextQuranPage();
            }
        } else if (audioStateEvent.getAudioState() == AudioStateEvent.State.PLAY_PREV) {
            if (quranPageFragment.getCurrentAyaIndex() != 0) {
                quranPageFragment.drawActionShadow(true);
                checkAyaRecorderState(quranPageFragment.getCurrentAyaId());
            } else {
                swipToPrevQuranPage();
            }
        } else if (audioStateEvent.getAudioState() == AudioStateEvent.State.STOP) {
            onClickStop();
        } else if (audioStateEvent.getAudioState() == AudioStateEvent.State.COMPLETED) {
            if (quranPageFragment.getCurrentAyaIndex() == quranPageFragment.getNumOfAyaInPage() - 1) {
                swipToNextQuranPage();
            }  // play audio of next aya after current aya audio was finished
            else {
                quranPageFragment.drawActionShadow(false);
                checkAyaRecorderState(quranPageFragment.getCurrentAyaId());
            }
        } else if (audioStateEvent.getAudioState() == AudioStateEvent.State.NOT_DOWNLOADED) {
            quranPageFragment.onAyaAudioNotFound();
        } else if (audioStateEvent.getAudioState() == AudioStateEvent.State.GROUP_REPEAT_COMPLETED) {
            swipToFirstAyaInRepeatGroup();
        }
    }
}
