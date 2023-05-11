package app.quranhub.ui.mushaf.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.databinding.FragmentSuraIndexBinding
import app.quranhub.ui.mushaf.adapter.SuraIndexAdapter
import app.quranhub.ui.mushaf.listener.ItemSelectionListener
import app.quranhub.ui.mushaf.model.SuraIndexModelMapper

class SuraIndexFragment : Fragment(), ItemSelectionListener<Int> {

    private var binding: FragmentSuraIndexBinding? = null

    private var adapter: SuraIndexAdapter? = null
    private val fastScrollerAnimator: ViewPropertyAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSuraIndexBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
    }

    private fun initRecycler() {
        binding!!.suraIndexRv.layoutManager = LinearLayoutManager(activity)
        adapter = SuraIndexAdapter(requireActivity(), this)
        binding!!.suraIndexRv.adapter = adapter
    }

    fun onSearchSura(inputQuery: String?) {
        adapter!!.filter(inputQuery!!)
    }

    override fun onSelectItem(suraPage: Int) {
        val parentFragment = parentFragment
        if (parentFragment is SuraGuz2IndexFragment) {
            parentFragment.navigateToSelectedSura(suraPage)
        }
    }

    fun setAdapterData(indexList: List<SuraIndexModelMapper>) {
        adapter!!.setSuraIndexModelList(indexList)
        binding!!.suraIndexRv.recycledViewPool.clear()
    }
}