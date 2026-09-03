package app.quranhub.ui.first_wizard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.databinding.FragmentOptionsListBinding
import app.quranhub.ui.common.interfaces.Searchable
import app.quranhub.ui.first_wizard.OptionsListFragment.OnOptionClickListener

/**
 * Activities that contain this fragment must implement the
 * [OnOptionClickListener] interface
 * to handle interaction events.
 * Use the [OptionsListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OptionsListFragment : Fragment(), OptionsListAdapter.ItemClickListener, Searchable {

    private var requestCode = 0
    private lateinit var options: List<String>
    private var optionsThumbnailsDrawableIds: IntArray? = null
    private var selectedOptionPosition = 0
    private val viewModel: FirstTimeWizardViewModel by activityViewModels()
    private var listener: OnOptionClickListener? = null
    private var optionsListAdapter: OptionsListAdapter? = null
    private var searchText: String? = ""
    private var binding: FragmentOptionsListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            options = it.getStringArrayList(ARG_OPTIONS)!!
            optionsThumbnailsDrawableIds = it.getIntArray(ARG_OPTIONS_THUMBNAILS)
            selectedOptionPosition = it.getInt(ARG_SELECTED_OPTION_POSITION)
            requestCode = it.getInt(ARG_REQUEST_CODE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOptionsListBinding.inflate(inflater, container, false)
        initOptionsRecyclerView()
        return binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.let {
            val search = it.getString(STATE_SEARCH_TEXT, null)
            search?.let {
                searchText = search
                search(searchText)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (!searchText.isNullOrEmpty()) {
            outState.putString(STATE_SEARCH_TEXT, searchText)
        }
    }

    private fun initOptionsRecyclerView() {
        binding!!.rvOptions.setHasFixedSize(true)
        binding!!.rvOptions.layoutManager = LinearLayoutManager(
            context, RecyclerView.VERTICAL, false
        )
        val dividerItemDecoration = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        binding!!.rvOptions.addItemDecoration(dividerItemDecoration)
        optionsListAdapter = OptionsListAdapter(
            options, optionsThumbnailsDrawableIds, currentSelectedOptionIndex(), this
        )
        binding!!.rvOptions.adapter = optionsListAdapter
    }

    /**
     * The currently selected option index for this step. Read from the shared
     * wizard ViewModel so a restored fragment (whose arguments are frozen at
     * creation time) still reflects selections made before a configuration change.
     */
    private fun currentSelectedOptionIndex(): Int = when (requestCode) {
        FirstTimeWizardActivity.RC_APP_LANGUAGES_STEP ->
            viewModel.uiState.value.appLangIndex

        FirstTimeWizardActivity.RC_TRANSLATION_LANGUAGES_STEP ->
            viewModel.uiState.value.translationLangIndex

        FirstTimeWizardActivity.RC_RECITATIONS_STEP ->
            viewModel.uiState.value.recitationIndex

        else -> selectedOptionPosition
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is OnOptionClickListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnOptionClickListener"
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onItemClick(clickedItemIndex: Int) {
        listener!!.onOptionClicked(requestCode, options[clickedItemIndex], clickedItemIndex)
    }

    override fun search(text: String?) {
        searchText = text
        optionsListAdapter!!.filter.filter(text)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnOptionClickListener {
        fun onOptionClicked(requestCode: Int, option: String, position: Int)
    }

    companion object {

        private val TAG = OptionsListFragment::class.java.simpleName

        private const val STATE_SEARCH_TEXT = "STATE_SEARCH_TEXT"

        private const val ARG_OPTIONS = "ARG_OPTIONS"
        private const val ARG_OPTIONS_THUMBNAILS = "ARG_OPTIONS_THUMBNAILS"
        private const val ARG_SELECTED_OPTION_POSITION = "ARG_SELECTED_OPTION_POSITION"
        private const val ARG_REQUEST_CODE = "ARG_REQUEST_CODE"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        fun newInstance(
            options: ArrayList<String>,
            optionsThumbnailsDrawableIds: IntArray?,
            selectedOptionPosition: Int,
            requestCode: Int
        ): OptionsListFragment {
            val fragment = OptionsListFragment()
            val args = Bundle()
            args.putStringArrayList(ARG_OPTIONS, options)
            args.putIntArray(ARG_OPTIONS_THUMBNAILS, optionsThumbnailsDrawableIds)
            args.putInt(ARG_SELECTED_OPTION_POSITION, selectedOptionPosition)
            args.putInt(ARG_REQUEST_CODE, requestCode)
            fragment.arguments = args
            return fragment
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        fun newInstance(
            context: Context,
            optionsStrResIds: IntArray,
            selectedOptionPosition: Int,
            requestCode: Int
        ): OptionsListFragment {
            val options = ArrayList<String>()
            for (strResId in optionsStrResIds) {
                options.add(context.getString(strResId))
            }
            return newInstance(options, null, selectedOptionPosition, requestCode)
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        fun newInstance(
            context: Context,
            optionsStrResIds: IntArray,
            optionsThumbnailsDrawableIds: IntArray?,
            selectedOptionPosition: Int,
            requestCode: Int
        ): OptionsListFragment {
            val options = ArrayList<String>()
            for (strResId in optionsStrResIds) {
                options.add(context.getString(strResId))
            }
            return newInstance(
                options,
                optionsThumbnailsDrawableIds,
                selectedOptionPosition,
                requestCode
            )
        }
    }
}