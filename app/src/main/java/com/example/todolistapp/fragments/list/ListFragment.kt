package com.example.todolistapp.fragments.list

import android.app.AlertDialog

import android.os.Bundle

import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.example.todolistapp.R
import com.example.todolistapp.data.models.ToDoData
import com.example.todolistapp.data.viewmodel.ToDoViewModel
import com.example.todolistapp.databinding.FragmentListBinding
import com.example.todolistapp.fragments.SharedViewModel
import com.example.todolistapp.fragments.list.adapter.ListAdapter
import com.example.todolistapp.utils.hideKeyboard
import com.example.todolistapp.utils.observeOnce
import com.google.android.material.snackbar.Snackbar

class ListFragment : Fragment(),SearchView.OnQueryTextListener{
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel:SharedViewModel by viewModels()

    private val adapter :ListAdapter by lazy { ListAdapter() }

    private var _binding:FragmentListBinding?=null
    private val binding get() = _binding!!



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Data Binding
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner=this
        binding.mSharedViewModel=mSharedViewModel


        // Setup RecyclerView
        setupRecyclerview()

         //Observe LiveData
        mToDoViewModel.getAllData.observe(viewLifecycleOwner, Observer { data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
            adapter.setData(data)
            binding.recyclerView.scheduleLayoutAnimation()
        })

        //Hide Keyboard
        hideKeyboard(requireActivity())

        return _binding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost:MenuHost=requireActivity()
        menuHost.addMenuProvider(object :MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.list_fragment_menu,menu)

                val search = menu.findItem(R.id.menu_search)
                val searchView=search.actionView as? SearchView
                searchView?.isSubmitButtonEnabled=true
                searchView?.setOnQueryTextListener(this@ListFragment)

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.menu_delete_all ->  confirmRemoval()
                    R.id.priority_high ->
                        mToDoViewModel.sortByHighPriority.observe(viewLifecycleOwner) {
                            adapter.setData(it) }
                    R.id.priority_low ->
                        mToDoViewModel.sortByLowPriority.observe(viewLifecycleOwner) {
                            adapter.setData(it)
                        }
                    android.R.id.home -> requireActivity().onBackPressed()
                }

                return true

            }

        },viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerview() {
        val recyclerView=binding.recyclerView
        recyclerView.adapter=adapter
        recyclerView.layoutManager=StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)


        //Swipe to delete

        swipeToDelete(recyclerView)

    }

    private fun swipeToDelete(recyclerView: RecyclerView){
        val swipeToDeleteCallback=object :SwipeToDelete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val deleteItem=adapter.dataList[viewHolder.adapterPosition]
                mToDoViewModel.deleteItem(deleteItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)

                restoreDeletedData(viewHolder.itemView, deleteItem)
            }

        }
        val itemTouchHelper=ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }

        private fun restoreDeletedData(view: View,deletedItem: ToDoData){
            val snackBar =Snackbar.make(view,"Deleted '${deletedItem.title}'",Snackbar.LENGTH_LONG)

            snackBar.setAction("Undo"){
                mToDoViewModel.insertData(deletedItem)

            }
            snackBar.show()

        }


    override fun onQueryTextSubmit(query: String?): Boolean {

        if (query !=null){
            searchTroughDatabase(query)
        }
        return true
    }


    override fun onQueryTextChange(query: String?): Boolean {
        if (query !=null){
            searchTroughDatabase(query)
        }
        return true
    }

    private fun searchTroughDatabase(query: String) {
      val searchQuery  = "%$query%"


        mToDoViewModel.searchDatabase(searchQuery).observeOnce(viewLifecycleOwner, Observer {list ->
            list?.let {
                adapter.setData(it)

            }

        })
    }


    // Show AlertDialog to Confirm Removal of All Items from Database Tabl

    private fun confirmRemoval() {
        val builder= AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes"){ _,_ ->
            mToDoViewModel.deleteAll()
            Toast.makeText(requireContext(),"Successfully Removed Everything!",
                Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton( "No"){_,_ -> }
        builder.setTitle("Delete Everything")
        builder.setMessage("Are you sure you want to remove everything")
        builder.create().show()


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }


}







