package app.quranhub.ui.mushaf.fragments;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.entity.TranslationBook;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.databinding.FragmentTafseerBinding;
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment;
import app.quranhub.ui.common.interfaces.ToolbarActionsListener;
import app.quranhub.ui.mushaf.adapter.TafseerAdapter;
import app.quranhub.ui.mushaf.dialogs.OptionDialog;
import app.quranhub.ui.mushaf.dialogs.TranslationsDialogFragment;
import app.quranhub.ui.mushaf.model.TafseerModel;
import app.quranhub.ui.mushaf.viewmodel.TafseerViewModel;

public class TafseerFragment extends Fragment implements OptionDialog.ItemClickListener
        , TranslationsDataFragment.TranslationSelectionListener
        , OptionsListDialogFragment.ItemSelectionListener {

    private FragmentTafseerBinding binding;

    private static final int RC_TRANS_LANG_SETTING = 2;
    private String inputSearch = "";
    private ToolbarActionsListener navDrawerListener;
    private static final String ARG_SURA_NAME = "ARG_SURA_NAME";
    private static final String ARG_SURA_NUMBER = "ARG_PAGE_NUMBER";
    private static final String ARG_BOOK_DB_NAME = "ARG_BOOK_DB_NAME";
    private static final String ARG_BOOK_NAME = "ARG_BOOK_NAME";
    private static final String ARG_AYA_NUMBER = "ARG_AYA_NUMBER";
    private String suraName;
    private String bookDbName, bookName;
    private int suraNumber, ayaNumber;
    private TafseerAdapter adapter;
    private TafseerViewModel viewModel;
    private String currentTafsserId;
    private String currentTafseerLang;
    private List<TafseerModel> ayasTafseer;

    public static TafseerFragment newInstance(String suraName, int suraumber, String bookDbName, String bookName, int ayaNumber) {
        TafseerFragment tafseerFragment = new TafseerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SURA_NAME, suraName);
        bundle.putInt(ARG_SURA_NUMBER, suraumber);
        bundle.putString(ARG_BOOK_DB_NAME, bookDbName);
        bundle.putString(ARG_BOOK_NAME, bookName);
        bundle.putInt(ARG_AYA_NUMBER, ayaNumber);
        tafseerFragment.setArguments(bundle);
        return tafseerFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ToolbarActionsListener) {
            navDrawerListener = (ToolbarActionsListener) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTafseerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getArgumentsData();

        if (savedInstanceState != null) {
            getPrevState(savedInstanceState);
        }
        initRecycler();
        bindViewModel();

        attachListeners();
    }

    private void attachListeners() {
        observeOnInputSearch();

        binding.filterSuraBtn.setOnClickListener(v -> onOpenSuraFilter());
        binding.filterBookBtn.setOnClickListener(v -> onOpenBooksFilter());
        binding.filterLangBtn.setOnClickListener(v -> onOpenLangFilter());
        binding.hamburgerIv.setOnClickListener(v -> onNavHamburgerClick());
    }

    private void getPrevState(Bundle savedInstanceState) {
        inputSearch = savedInstanceState.getString("input_search");
        suraName = savedInstanceState.getString("sura_name");
        suraNumber = savedInstanceState.getInt("sura_number");
        binding.suraTv.setText(suraName);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("input_search", inputSearch);
        outState.putString("sura_name", suraName);
        outState.putInt("sura_number", suraNumber);
    }

    private void initRecycler() {
        adapter = new TafseerAdapter(getActivity());
        binding.tafseerRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.tafseerRv.setHasFixedSize(true);
        binding.tafseerRv.setAdapter(adapter);
    }

    private void bindViewModel() {

        viewModel = new ViewModelProvider(this).get(TafseerViewModel.class);
        if (currentTafsserId != null || currentTafseerLang.equals("ar")) {
            getTafseers();
        } else {
            binding.progreesBar.setVisibility(View.GONE);
            binding.bookTv.setText(getString(R.string.choose_book));
            Toast.makeText(getActivity(), getString(R.string.no_downloaded_books), Toast.LENGTH_LONG).show();
        }

        viewModel.getTafseers().observe(getViewLifecycleOwner(), tafseerModels -> {

            binding.progreesBar.setVisibility(View.GONE);
            if (tafseerModels != null && tafseerModels.size() > 0) {
                adapter.setTafseerModelList(tafseerModels);
                if (ayaNumber <= tafseerModels.size()) {
                    binding.tafseerRv.scrollToPosition(ayaNumber - 1);
                } else {
                    binding.tafseerRv.scrollToPosition(0);
                }
                if (inputSearch != null && !TextUtils.isEmpty(inputSearch.trim())) {
                    adapter.filter(inputSearch);
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.data_failed), Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getAyahs().observe(getViewLifecycleOwner(), tafseerModels -> {
            ayasTafseer = tafseerModels;
            viewModel.getSuraTafseers(bookDbName, suraNumber);
        });

        viewModel.getBookTafseers().observe(getViewLifecycleOwner(), translations -> {
            binding.progreesBar.setVisibility(View.GONE);
            if (translations != null && ayasTafseer != null && translations.size() > 0) {
                List<TafseerModel> tafseerModels = TafseerModel.map(translations, ayasTafseer);
                adapter.setTafseerModelList(tafseerModels);
                if (ayaNumber <= tafseerModels.size()) {
                    binding.tafseerRv.scrollToPosition(ayaNumber - 1);
                } else {
                    binding.tafseerRv.scrollToPosition(0);
                }
                if (inputSearch != null && !TextUtils.isEmpty(inputSearch.trim())) {
                    adapter.filter(inputSearch);
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.data_failed), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void getTafseers() {
        if (currentTafsserId == null && currentTafseerLang.equals("ar")) {         // get aya tafseer from default book ("EL-Meyser")
            viewModel.getSuraTafseers(suraNumber);
        } else {
            viewModel.getSuraAyahs(suraNumber);
        }
    }

    private void observeOnInputSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                inputSearch = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void getArgumentsData() {
        currentTafsserId = AppPreferencesManager.getQuranTranslationBook(requireActivity());
        currentTafseerLang = AppPreferencesManager.getQuranTranslationLanguage(requireActivity());
        if (getArguments() != null) {
            suraName = getArguments().getString(ARG_SURA_NAME);
            suraNumber = getArguments().getInt(ARG_SURA_NUMBER);
            bookDbName = getArguments().getString(ARG_BOOK_DB_NAME);
            bookName = getArguments().getString(ARG_BOOK_NAME);
            ayaNumber = getArguments().getInt("ARG_AYA_NUMBER");
            binding.bookTv.setText(bookName);
            binding.suraTv.setText(suraName);
        }
        int currentTranslationLanguageIndex = Constants.Language.CODES.indexOf(
                AppPreferencesManager.getQuranTranslationLanguage(requireContext()));
        binding.langTv.setText(
                getString(Constants.Language.NAMES_STR_IDS[currentTranslationLanguageIndex]));
    }

    private void onOpenSuraFilter() {
        String[] optionsArr = getResources().getStringArray(R.array.sura_name);
        ArrayList<String> options = new ArrayList<>();
        options.addAll(Arrays.asList(optionsArr));
        DialogFragment fragment = OptionDialog.getInstance(options, suraName, 1
                , getString(R.string.suras));
        fragment.show(getChildFragmentManager(), "trans_sura_dialog");
    }

    private void onOpenBooksFilter() {
        String transLang = AppPreferencesManager.getQuranTranslationLanguage(requireContext());
        TranslationsDialogFragment translationsDialog = TranslationsDialogFragment.newInstance(
                transLang, this);
        translationsDialog.show(getParentFragmentManager(), "trans_book_dialog");
    }

    private void onOpenLangFilter() {
        int currentTranslationLanguageIndex = Constants.Language.CODES.indexOf(
                AppPreferencesManager.getQuranTranslationLanguage(requireContext()));
        OptionsListDialogFragment translationLangDialog = OptionsListDialogFragment.getInstance(
                getString(R.string.translation_lang_setting_dialog_title),
                Constants.Language.NAMES_STR_IDS, currentTranslationLanguageIndex
                , this, RC_TRANS_LANG_SETTING);
        translationLangDialog.show(getParentFragmentManager(), "trans_lang_dialog");
    }

    private void onNavHamburgerClick() {
        navDrawerListener.onNavDrawerClick();
    }

    @Override
    public void onTranslationSelected(TranslationBook translationBook) {
        bookDbName = translationBook.getDatabaseName();
        getTafseers();
        //viewModel.getSuraTafseers(translationBook.getDatabaseName(), suraNumber);
        currentTafsserId = translationBook.getId();
        binding.bookTv.setText(translationBook.getName());
    }

    @Override
    public void onItemSelected(int requestCode, int itemIndex) {
        String langCode = Constants.Language.CODES.get(itemIndex);
        AppPreferencesManager.persistQuranTranslationLanguage(requireContext(), langCode);
        currentTafseerLang = langCode;
        binding.langTv.setText(getString(Constants.Language.NAMES_STR_IDS[itemIndex]));
    }

    @Override
    public void onItemClick(String optionName, int optionIndex, int requestCode) {
        binding.etSearch.getText().clear();
        this.suraName = optionName;
        this.suraNumber = optionIndex + 1;
        this.ayaNumber = 1;
        getTafseers();
        binding.suraTv.setText(suraName);
    }

}
