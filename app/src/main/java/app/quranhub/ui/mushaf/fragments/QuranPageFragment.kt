package app.quranhub.ui.mushaf.fragments;


import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.entity.Aya;
import app.quranhub.data.local.entity.AyaBookmark;
import app.quranhub.data.local.entity.BookmarkType;
import app.quranhub.data.local.entity.Note;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.data.model.ReciterModel;
import app.quranhub.databinding.FragmentQuranPageBinding;
import app.quranhub.ui.downloads_manager.dialogs.AudioDownloadAmountDialogFragment;
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment;
import app.quranhub.ui.mushaf.dialogs.AddBookmarkDialog;
import app.quranhub.ui.mushaf.dialogs.AddNoteDialog;
import app.quranhub.ui.mushaf.dialogs.AyaActionsDialog;
import app.quranhub.ui.mushaf.model.BookmarkModel;
import app.quranhub.ui.mushaf.presenter.QuranPagePresenter;
import app.quranhub.ui.mushaf.presenter.QuranPagePresenterImp;
import app.quranhub.ui.mushaf.view.QuranPageView;
import app.quranhub.util.FragmentUtils;
import app.quranhub.util.GlideApp;
import app.quranhub.util.ImageUtil;
import app.quranhub.util.IntentUtils;
import app.quranhub.util.ScreenUtils;

public class QuranPageFragment extends Fragment
        implements AyaActionsDialog.AyaPropertiesListener, AddNoteDialog.AddNoteListener,
        QuranPageView, AddBookmarkDialog.AddBookmarkListener,
        QuranRecitersDialogFragment.ReciterSelectionListener, AudioDownloadAmountDialogFragment.AudioDownloadListener {

    private static final String TAG = QuranPageFragment.class.getSimpleName();

    private static final String ARG_QURAN_PAGE_NUM = "ARG_QURAN_PAGE_NUM";
    private static final String ARG_QURAN_IMAGE_URL = "ARG_QURAN_IMAGE_URL";
    private static final String ARG_INIT_SELECTED_AYA_ID = "ARG_INIT_SELECTED_AYA_ID";
    private static final String ARG_NIGHT_MODE = "ARG_NIGHT_MODE";

    private FragmentQuranPageBinding binding;

    private MushafFragment mushafFragment;
    private String quranImageUrl;
    private int quranPageNum;
    private int initSelectedAyaId;
    private boolean nightMode = false;
    private int numOfAyaInPage;
    private boolean isPageShown = false, noteDialogOpen = false;
    private int quranImageContainerHeight, quranImageContainerWidth;
    private Bundle ayaActionsArgs;
    private AyaActionsDialog ayaActionsDialog;
    private AddBookmarkDialog bookmarkDialog;
    private int longClickXlocation, longClickYlocation;
    private Aya currentAya, previousAya;
    private int currentAyaIndex;
    private double imageScaleFactor;
    private List<Aya> pageAyasList;
    private List<View> ayaShadowsViews;
    private QuranPagePresenter presenter;
    private int recitationId;
    private int margin;
    private double start, lineHeight, top, end;
    private double xfactor, xoffset, yfactor, yoffset;
    private boolean isAyaBookmark = false, playFirstAyaAudio = false, playMiddleAyaAudio = false, ayaAudioDownloaded = false;
    private Note selectedAyaNote;
    private boolean isVisibleToUser;
    private boolean drawShadowFromNotification = false;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param quranImageUrl
     * @param quranPageNum
     * @param initSelectedAyaId
     * @param nightMode
     * @return A new instance of fragment @{@link QuranPageFragment}
     */
    public static QuranPageFragment getInstance(@NonNull String quranImageUrl, int quranPageNum
            , int initSelectedAyaId, boolean nightMode) {
        Log.d(TAG, "Loading page number: " + quranPageNum);
        QuranPageFragment fragment = new QuranPageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_QURAN_IMAGE_URL, quranImageUrl);
        bundle.putInt(ARG_QURAN_PAGE_NUM, quranPageNum);
        bundle.putInt(ARG_INIT_SELECTED_AYA_ID, initSelectedAyaId);
        bundle.putBoolean(ARG_NIGHT_MODE, nightMode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            quranImageUrl = getArguments().getString(ARG_QURAN_IMAGE_URL);
            quranPageNum = getArguments().getInt(ARG_QURAN_PAGE_NUM);
            initSelectedAyaId = getArguments().getInt(ARG_INIT_SELECTED_AYA_ID, -1);
            nightMode = getArguments().getBoolean(ARG_NIGHT_MODE, false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQuranPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (nightMode) {
            binding.root.setBackgroundColor(Color.BLACK);
        }

        setParentFragment();
        recitationId = AppPreferencesManager.getRecitationSetting(requireContext());
        getCurrentPageAyas();
        binding.containerSv.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (binding.containerSv.getViewTreeObserver() != null)
                            binding.containerSv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        // get quran iv container width and lineHeight
                        quranImageContainerWidth = binding.containerSv.getWidth();
                        quranImageContainerHeight = binding.containerSv.getHeight();
                        Log.d(TAG, "quran image container: " + quranImageContainerWidth + " , " + quranImageContainerHeight);
                        scaleQuranImage();
                        calculateImageMetrics();
                        ayaActionsArgs = new Bundle();
                        initOnLongClickQuranPage();

                        if (savedInstanceState == null) {
                            initBookmarkDialog();
                        } else {
                            getPrevSavedInstance(savedInstanceState);
                        }
                        showQuranPage();
                    }
                });

        binding.pageIv.setOnClickListener(v -> onQuranPageClick());
    }

    private void setParentFragment() {
        if (mushafFragment == null) {
            mushafFragment = (MushafFragment) getParentFragment();
        }
    }


    @Override
    public void drawInitAyaShadow(@NonNull Aya aya, @Nullable Aya previousAya) {
        isAyaBookmark = false;
        this.currentAya = aya;
        this.previousAya = previousAya;
        drawShadow();
    }

    private void getPrevSavedInstance(Bundle savedInstanceState) {

        if (savedInstanceState != null) {

            numOfAyaInPage = savedInstanceState.getInt("num_of_ayas");
            currentAya = savedInstanceState.getParcelable("current_aya");
            previousAya = savedInstanceState.getParcelable("prev_aya");
            currentAyaIndex = savedInstanceState.getInt("CURRENT_AYA_INDEX");
            ayaAudioDownloaded = savedInstanceState.getBoolean("AYA_AUDIO_DOWNLOADED");
            ayaActionsDialog = (AyaActionsDialog) getChildFragmentManager().findFragmentByTag("AyaActionsDialog");
            bookmarkDialog = (AddBookmarkDialog) getChildFragmentManager().findFragmentByTag("AddBookmarkDialog");

            if (currentAya != null) {
                presenter.getAyaBookmarkType(currentAya.getId());
                presenter.checkAyaHasNote(currentAya.getId());
                int currentAyaY;
                switch (recitationId) {
                    case Constants.Recitation.HAFS_ID:
                        currentAyaY = currentAya.getY();
                        break;
                    case Constants.Recitation.WARSH_ID:
                        currentAyaY = currentAya.getYw();
                        break;
                    default:
                        throw new RuntimeException("Cannot identify recitation");
                }

                // scroll to selected aya after landscape orientation
                binding.containerSv.post(() -> {
                    binding.containerSv.scrollTo(0, (int) (currentAyaY * imageScaleFactor));
                });

            }

            noteDialogOpen = savedInstanceState.getBoolean("open_dialog");
            if (noteDialogOpen) {
                noteDialogOpen = false;
                selectedAyaNote = savedInstanceState.getParcelable("selected_note");
                openAddNoteDialog();
            }

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("current_aya", currentAya);
        outState.putParcelable("prev_aya", previousAya);
        outState.putParcelable("selected_note", selectedAyaNote);
        outState.putBoolean("open_dialog", noteDialogOpen);
        outState.putInt("CURRENT_AYA_INDEX", currentAyaIndex);
        outState.putInt("num_of_ayas", numOfAyaInPage);
        outState.putBoolean("AYA_AUDIO_DOWNLOADED", ayaAudioDownloaded);
    }

    private void calculateImageMetrics() {
        if (recitationId == Constants.Recitation.WARSH_ID) {
            lineHeight = ((quranPageNum == 1 || quranPageNum == 2) ? 37 : 63) * imageScaleFactor;
            end = ((quranPageNum == 1 || quranPageNum == 2) ? 454 : 580) * imageScaleFactor;
            start = (quranPageNum == 2 ? 148 : quranPageNum == 1 ? 170 : 41) * imageScaleFactor;
            top = (quranPageNum == 1 || quranPageNum == 2 ? 378 : 42) * imageScaleFactor;
            xfactor = (quranPageNum == 1 || quranPageNum == 2) ? 1.377d : 1.367d;
            xoffset = (quranPageNum == 1 || quranPageNum == 2) ? 13.424d : 18.921d;
            yfactor = (quranPageNum == 1 || quranPageNum == 2) ? 1.365d : 1.359d;
            yoffset = (quranPageNum == 1 || quranPageNum == 2) ? 15.462d : 25.375d;
            margin = ((quranPageNum == 1 || quranPageNum == 2) ? 8 : 5);

        } else if (recitationId == Constants.Recitation.HAFS_ID) {
            lineHeight = ((quranPageNum == 1 || quranPageNum == 2) ? 38 : 53) * imageScaleFactor;
            end = ((quranPageNum == 1 || quranPageNum == 2) ? 454 : 554) * imageScaleFactor;
            start = ((quranPageNum == 1 || quranPageNum == 2) ? 170 : 70) * imageScaleFactor;
            top = ((quranPageNum == 1 ? 389 : quranPageNum == 2 ? 418 : 85)) * imageScaleFactor;
            xfactor = 1.367d;
            xoffset = 13.827d;
            yfactor = 1.369d;
            yoffset = 20.723d;
            margin = ((quranPageNum == 1 || quranPageNum == 2) ? 4 : 9);
        } else {
            throw new RuntimeException("Cannot identify recitation");
        }
    }

    private double getScaledY(int y, boolean withHeight) {
        return (yfactor * y - yoffset - margin) * imageScaleFactor + (withHeight ? lineHeight : 0);
    }

    private double getScaledX(int x) {
        return (xfactor * x - xoffset) * imageScaleFactor;
    }

    private void getCurrentPageAyas() {
        ayaShadowsViews = new ArrayList<>();
        presenter = new QuranPagePresenterImp(getActivity());
        presenter.onAttach(this);
        presenter.getPageAyas(quranPageNum);
    }

    private void initBookmarkDialog() {
        bookmarkDialog = new AddBookmarkDialog();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnLongClickQuranPage() {

        binding.pageIv.setOnTouchListener((v, event) -> {
            longClickXlocation = (int) event.getX();
            longClickYlocation = (int) event.getY();
            return false;
        });

        binding.pageIv.setOnLongClickListener(v -> {
            if (!isPageShown)
                return false;
            isAyaBookmark = false;
            ayaAudioDownloaded = false;
            selectedAyaNote = null;
            getSelectedAya();
            if (currentAya != null) {
                drawShadow();
                showActionsDialog();
            } else {
                removePrevAyaShadows();
            }
            return true;
        });
    }


    private void showActionsDialog() {
        /* the dialog coordinates itself to the window origin,
           instead we want it to coordinate to the quran image origin */

        int yLocation = ScreenUtils.getStatusBarHeight(requireContext(), binding.pageIv); // base yLocation
        int y;
        switch (recitationId) {
            case Constants.Recitation.HAFS_ID:
                y = currentAya.getY();
                break;
            case Constants.Recitation.WARSH_ID:
                y = currentAya.getYw();
                break;
            default:
                throw new RuntimeException("Cannot identify recitation");
        }
        yLocation += getScaledY(y, false) + (1.2 * lineHeight);
        ayaActionsDialog = new AyaActionsDialog();
        ayaActionsArgs.putInt(AyaActionsDialog.ARG_Y_LOCATION, yLocation);
        ayaActionsDialog.setArguments(ayaActionsArgs);
        ayaActionsDialog.show(getChildFragmentManager(), "AyaActionsDialog");

        presenter.getAyaBookmarkType(currentAya.getId());
        presenter.checkAyaHasNote(currentAya.getId());
    }

    private void removePrevAyaShadows() {
        for (View view : ayaShadowsViews) {
            binding.quranPageContainer.removeView(view);
        }
        ayaShadowsViews.clear();
    }


    private void getSelectedAya() {
        currentAya = null;
        previousAya = null;
        if (pageAyasList != null && pageAyasList.size() > 0) {
            double ayaX, ayaY;
            Aya prev = null;
            for (int i = 0; i < pageAyasList.size(); i++) {
                Aya aya = pageAyasList.get(i);
                int x, y;
                switch (recitationId) {
                    case Constants.Recitation.HAFS_ID:
                        x = aya.getX();
                        y = aya.getY();
                        break;
                    case Constants.Recitation.WARSH_ID:
                        x = aya.getXw();
                        y = aya.getYw();
                        break;
                    default:
                        throw new RuntimeException("Cannot identify recitation");
                }
                ayaX = getScaledX(x);
                ayaY = getScaledY(y, true);


                if (longClickYlocation <= ayaY) {
                    if ((ayaY - longClickYlocation <= lineHeight) && longClickXlocation < ayaX) {
                        prev = aya;
                        continue;
                    }
                    // We have found the clicked Aya
                    currentAyaIndex = i;
                    currentAya = aya;
                    previousAya = prev;

                    Log.d(TAG, "currentAya: " + currentAya);
                    Log.d(TAG, "previousAya: " + previousAya);

                    break;
                }
                prev = aya;
            }

        } else {
            Toast.makeText(getActivity(), R.string.select_aya_fail, Toast.LENGTH_LONG).show();
        }

    }


    private void scaleQuranImage() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.pageIv.getLayoutParams();
        int quranPageOriginalWidth, quranPageOriginalHeight;
        switch (recitationId) {
            case Constants.Recitation.HAFS_ID:
                quranPageOriginalWidth = Constants.Quran.HAFS_OTLOOHA_PAGE_ORIGINAL_WIDTH;
                quranPageOriginalHeight = Constants.Quran.HAFS_OTLOOHA_PAGE_ORIGINAL_HEIGHT;
                break;
            case Constants.Recitation.WARSH_ID:
                quranPageOriginalWidth = Constants.Quran.WARSH_OTLOOHA_PAGE_ORIGINAL_WIDTH;
                quranPageOriginalHeight = Constants.Quran.WARSH_OTLOOHA_PAGE_ORIGINAL_HEIGHT;
                break;
            default:
                throw new RuntimeException("Cannot identify recitation");
        }

        // make width fit in mobile screen and lineHeight scale
        if (quranPageOriginalWidth != quranImageContainerWidth) {
            imageScaleFactor = (float) quranImageContainerWidth / quranPageOriginalWidth;
            params.width = quranImageContainerWidth;
            params.height = (int) (quranPageOriginalHeight * imageScaleFactor);
            binding.pageIv.setLayoutParams(params);
        }

        // handle if lineHeight will be bigger than container lineHeight when above "if" is true
        if (getContext() != null && ScreenUtils.isPortrait(getContext()) && params.height > quranImageContainerHeight) {
            Log.d(TAG, "TRUE: portrait && params.lineHeight > quranImageContainerHeight");
            float ratioEdit = (float) quranImageContainerHeight / params.height;
            imageScaleFactor *= ratioEdit; // the total scale done on the image
            params.height = quranImageContainerHeight;
            params.width *= ratioEdit;
            binding.pageIv.setLayoutParams(params);
        }
        Log.d(TAG, "final ImageScaleFactor: " + imageScaleFactor);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        presenter.onDetach();
    }


    @Override
    public void onShareClick() {
        if (currentAya != null) {
            startActivity(IntentUtils.getShareIntent(currentAya.getText(), requireActivity()));
        }
    }

    @Override
    public void onFasilClick() {
        ayaActionsDialog.dismiss();
        if (isAyaBookmark) {                // remove from bookmark
            presenter.removeBookmark(currentAya.getId());
        } else {                        // add to bookmark
            presenter.getBookmarkTypes();
        }
    }


    @Override
    public void onGetAyaBookmarkTypes(List<BookmarkType> bookmarkTypes) {
        bookmarkDialog = AddBookmarkDialog.getInstance(bookmarkTypes, true);
        bookmarkDialog.show(getChildFragmentManager(), "AddBookmarkDialog");
    }


    @Override
    public void addNormalBookmark(int bookmarkType) {
        if (currentAya != null) {
            presenter.insertAyaBookmark(new AyaBookmark(currentAya.getId(), bookmarkType, currentAya));
        }
    }

    @Override
    public void addCustomBookmark(BookmarkType type) {
        if (currentAya != null) {
            presenter.insertCustomBookmark(currentAya, type);
        }
    }

    @Override
    public void onTafserClick() {
        Fragment fragment = getParentFragment();
        if (fragment instanceof MushafFragment) {
            ((MushafFragment) fragment).openTranslationDialog(
                    currentAya);
        }
    }

    @Override
    public void onNoteClick() {
        if (ScreenUtils.getOrientationState(requireActivity()).equals(ScreenUtils.PORTRAIT_STATE)) {
            openAddNoteDialog();
        } else {
            noteDialogOpen = true;
        }
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void openAddNoteDialog() {

        AddNoteDialog dialog;
        if (selectedAyaNote != null) {
            dialog = AddNoteDialog.getInstance(selectedAyaNote);
        } else {
            dialog = AddNoteDialog.getInstance(currentAya.getId());
        }
        dialog.show(getChildFragmentManager(), "AddNoteDialog");
    }

    public int getNumOfAyaInPage() {
        return numOfAyaInPage;
    }

    @Override
    public void onGetPageAya(List<Aya> ayaList) {
        pageAyasList = new ArrayList<>();
        pageAyasList.addAll(ayaList);
        numOfAyaInPage = pageAyasList.size();
        if (playFirstAyaAudio)
            checkPlayFirstAyaAudio();
        else if (playMiddleAyaAudio)
            checkPlayMiddleAyaAudio();
        else if (drawShadowFromNotification) {
            drawAyaNotificationShadow();
        }
    }


    @Override
    public void onGetAyaBookmarkType(BookmarkModel bookmarkModel) {
        if (ayaActionsDialog != null) {
            isAyaBookmark = true;
            ayaActionsDialog.setBookmarkTypeIcon(bookmarkModel);
        }
    }

    @Override
    public void onSuccessRemoveBookmark() {
        ayaActionsDialog.dismiss();
        Toast.makeText(getActivity(), getString(R.string.bookmark_removed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAyaHasNote(Note note) {
        if (note != null && ayaActionsDialog != null) {
            selectedAyaNote = note;
            ayaActionsDialog.setAyaHasNote();
        }
    }


    // draw shadow when user make prev or next action on aya
    public void drawActionShadow(boolean isClickPrev) {
        if (pageAyasList == null)
            return;
        if (isClickPrev) {
            if (currentAyaIndex - 1 > 0)
                previousAya = pageAyasList.get(currentAyaIndex - 2);
            else
                previousAya = null;
            currentAya = pageAyasList.get(currentAyaIndex - 1);
            currentAyaIndex--;
        } else {
            previousAya = pageAyasList.get(currentAyaIndex);
            currentAya = pageAyasList.get(currentAyaIndex + 1);
            currentAyaIndex++;
        }
        drawShadow();
    }

    // draw shadow on the selected aya
    private void drawShadow() {

        removePrevAyaShadows(); // remove any if exists

        View shadowView;
        RelativeLayout.LayoutParams params;

        double prevAyaY, prevAyaX;
        if (previousAya != null) {
            int prevX, prevY;
            switch (recitationId) {
                case Constants.Recitation.HAFS_ID:
                    prevY = previousAya.getY();
                    prevX = previousAya.getX();
                    break;
                case Constants.Recitation.WARSH_ID:
                    prevY = previousAya.getYw();
                    prevX = previousAya.getXw();
                    break;
                default:
                    throw new RuntimeException("Cannot identify recitation");
            }
            prevAyaY = getScaledY(prevY, false);
            prevAyaX = getScaledX(prevX);
        } else {
            prevAyaY = top;
            prevAyaX = end;
        }
        int curX, curY;
        switch (recitationId) {
            case Constants.Recitation.HAFS_ID:
                curX = currentAya.getX();
                curY = currentAya.getY();
                break;
            case Constants.Recitation.WARSH_ID:
                curX = currentAya.getXw();
                curY = currentAya.getYw();
                break;
            default:
                throw new RuntimeException("Cannot identify recitation");
        }
        double currentAyaY = getScaledY(curY, false);
        double currentAyaX = getScaledX(curX);

        double endWidth = end, startWidth = currentAyaX;
        boolean firstLine = true;
        // draw line from current aya line to prev aya

        while (currentAyaY >= (prevAyaY - lineHeight * 0.66)) {

            shadowView = new View(getActivity());
            ayaShadowsViews.add(shadowView);
            binding.quranPageContainer.addView(shadowView);
            params = (RelativeLayout.LayoutParams) shadowView.getLayoutParams();
            if (nightMode) {
                shadowView.setBackgroundColor(getResources().getColor(R.color.aya_shadow_color_night_mode));
            } else {
                shadowView.setBackgroundColor(getResources().getColor(R.color.aya_shadow_color));
            }

            if (!firstLine) {
                startWidth = (int) start;
            }
            if (currentAyaY - prevAyaY < lineHeight * 0.66) {
                endWidth = prevAyaX;
            }

            params.leftMargin = (int) startWidth;
            params.topMargin = (int) currentAyaY;
            params.width = (int) Math.abs(endWidth - startWidth);
            params.height = (int) (lineHeight);
            shadowView.setLayoutParams(params);
            currentAyaY -= lineHeight;
            firstLine = false;
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
        binding.progreesBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.progreesBar.setVisibility(GONE);
    }

    private void onQuranPageClick() {
        presenter.handleQuranPageClick();
    }

    public int getCurrentAyaIndex() {
        return currentAyaIndex;
    }


    public Aya getCurrentAya() {
        return currentAya;
    }

    public int getCurrentAyaId() {
        return currentAya.getId();
    }

    @Override
    public void onAddNote(Note note, boolean isEditable) {
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        presenter.addNote(note);
        if (isEditable) {
            Toast.makeText(getActivity(), getString(R.string.note_edited), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), getString(R.string.note_added), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDismissDialog() {
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


    private void showQuranPage() {

        GlideApp.with(requireActivity())
                .load(quranImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model
                            , Target<Drawable> target, boolean isFirstResource) {

                        Log.d(TAG, "onLoadFailed: GlideApp");
                        if (FragmentUtils.isSafeFragment(QuranPageFragment.this)) {
                            binding.progreesBar.setVisibility(GONE);
                            binding.loadFailedContainer.getRoot().setVisibility(View.VISIBLE);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model
                            , Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                        if (FragmentUtils.isSafeFragment(QuranPageFragment.this)) {
                            binding.progreesBar.setVisibility(GONE);
                            binding.loadFailedContainer.getRoot().setVisibility(GONE);
                            isPageShown = true;


                            if (nightMode) {
                                ImageUtil.invertDrawable(resource);
                            }

                            // auto play aya audio if audio player is playing  after image is loaded

                            if (playFirstAyaAudio && isVisibleToUser)
                                checkPlayFirstAyaAudio();
                            else if (playMiddleAyaAudio && isVisibleToUser)
                                checkPlayMiddleAyaAudio();
                            else if (drawShadowFromNotification && isVisibleToUser)
                                drawAyaNotificationShadow();
                            else {
                                if (currentAya == null) {
                                    presenter.drawInitAyaShadow(quranPageNum, initSelectedAyaId);
                                } else {
                                    // draw shadow if it is exist before orientation changed
                                    drawShadow();
                                }
                            }

                        }
                        return false;
                    }
                })
                .into(binding.pageIv);
    }

    public boolean isAyaAudioDownloaded() {
        return ayaAudioDownloaded;
    }

    public void setAyaAudioDownloaded(boolean ayaAudioDownloaded) {
        this.ayaAudioDownloaded = ayaAudioDownloaded;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        Activity activity = getActivity();
        if (activity != null && !activity.isChangingConfigurations() && !isVisibleToUser) {
            ayaAudioDownloaded = false;
        }
    }

    public void onAyaAudioNotFound() {
        mushafFragment.togglePauseState(false);
        ayaAudioDownloaded = false;

        String reciterId = AppPreferencesManager.getReciterSheikhSetting(requireContext());
        if (reciterId != null) {
            openDownloadAmountDialog(reciterId);
        } else {
            // user didn't choose any reciter yet
            openRecitersDialog();
        }
    }


    private void openDownloadAmountDialog(@NonNull String reciterId) {
        AudioDownloadAmountDialogFragment downloadAmountDialogFragment;
        if (currentAya != null) {
            downloadAmountDialogFragment = AudioDownloadAmountDialogFragment.newInstance(
                    recitationId, reciterId, currentAya.getSura());
        } else {
            downloadAmountDialogFragment = AudioDownloadAmountDialogFragment.newInstance(
                    recitationId, reciterId);
        }
        downloadAmountDialogFragment.show(getChildFragmentManager(), "AudioDownloadAmountDialogFragment");
    }

    private void openRecitersDialog() {
        QuranRecitersDialogFragment recitersDialogFragment = QuranRecitersDialogFragment
                .newInstance(recitationId);
        recitersDialogFragment.show(getChildFragmentManager(), "QuranRecitersDialogFragment");
    }

    @Override
    public void onListenClick() {
        mushafFragment.openAyaAudioDialog();
        mushafFragment.checkAyaRecorderState(currentAya.getId());
        mushafFragment.playAudioService();
    }

    // get aya position of start repeat interval in current page
    private int getFirstAyaNumberInPage() {
        if (pageAyasList.get(0).getSura() != mushafFragment.getFromSuraDownloaded()) {
            for (int i = 0; i < pageAyasList.size(); i++) {
                if (pageAyasList.get(i).getSura() == mushafFragment.getFromSuraDownloaded() && pageAyasList.get(i).getSuraAya() == mushafFragment.getFirstAyaInRepeatGroup()) {
                    return i;
                }
            }
        }
        return mushafFragment.getFirstAyaInRepeatGroup() - pageAyasList.get(0).getSuraAya();
    }

    // draw shadow of current aya played in notification audio when launch app from notification
    private void drawAyaNotificationShadow() {
        if (pageAyasList != null && drawShadowFromNotification && isPageShown) {
            drawShadowFromNotification = false;
            if (currentAya.getId() != pageAyasList.get(0).getId()) {
                for (int i = 1; i < pageAyasList.size(); i++) {
                    if (pageAyasList.get(i).getId() == currentAya.getId()) {
                        previousAya = pageAyasList.get(i - 1);
                        currentAyaIndex = i;
                        break;
                    }
                }
            } else {
                currentAyaIndex = 0;
                previousAya = null;
            }
            drawShadow();
        }
    }

    private void checkPlayFirstAyaAudio() {
        if (isPageShown && pageAyasList != null && playFirstAyaAudio) {
            currentAyaIndex = 0;
            currentAya = pageAyasList.get(currentAyaIndex);
            previousAya = null;
            drawShadow();
            mushafFragment.checkAyaRecorderState(currentAya.getId());
            playFirstAyaAudio = false;
            mushafFragment.playAudioService();
        }
    }

    private void checkPlayMiddleAyaAudio() {
        if (playMiddleAyaAudio && isPageShown && pageAyasList != null) {
            currentAyaIndex = getFirstAyaNumberInPage();
            currentAya = pageAyasList.get(currentAyaIndex);
            if (currentAyaIndex > 0)
                previousAya = pageAyasList.get(currentAyaIndex - 1);
            else
                previousAya = null;
            drawShadow();
            mushafFragment.checkAyaRecorderState(currentAya.getId());
            playMiddleAyaAudio = false;
            mushafFragment.playAudioService();
        }
    }

    public void playFirstAyaAudio() {
        playFirstAyaAudio = true;
        checkPlayFirstAyaAudio();
    }

    public void playMiddleAyaAudio() {
        playMiddleAyaAudio = true;
        checkPlayMiddleAyaAudio();
    }

    @Override
    public void onReciterSelected(int recitationId, @NonNull ReciterModel reciter) {
        openDownloadAmountDialog(reciter.getId());
    }

    @Override
    public void onClickDownload() {
        mushafFragment.setSelectedAyaAudio(currentAya);
    }

    public void setCurrentAyaFromNotification(Aya aya) {
        currentAya = aya;
        drawShadowFromNotification = true;
        drawAyaNotificationShadow();
    }

}
