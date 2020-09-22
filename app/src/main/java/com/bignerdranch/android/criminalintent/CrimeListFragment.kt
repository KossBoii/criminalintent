package com.bignerdranch.android.criminalintent

import android.content.Context
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.criminalintent.CrimeListFragment.Companion.newInstance
import kotlinx.android.synthetic.main.list_item_crime_police.*
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {

    /**
     *  Required interface for hosting activities
     */
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }
    private var callbacks: Callbacks? = null

    private lateinit var crimeRecyclerView: RecyclerView
//    private var adapter: CrimeAdapter? = null
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())
//    private var adapter: CrimeAdapter? = CrimeAdapter()

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.d(TAG, "Total crimes: ${crimeListViewModel.crimes.size}")
        setHasOptionsMenu(true)
    }


    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)

//        crimeRecyclerView.adapter = adapter

//        updateUI()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer {
                crimes -> crimes?.let {
                Log.i(TAG, "Got crimes ${crimes.size}")
                updateUI(crimes)
            }
        })
    }

//    private fun updateUI() {
//        val crimes = crimeListViewModel.crimes
//        adapter = CrimeAdapter(crimes)
//        crimeRecyclerView.adapter = adapter
//    }
    private fun updateUI(crimes: List<Crime>) {
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter

//        adapter?.submitList(null)
//        adapter?.submitList(crimes)
//        crimeRecyclerView.adapter?.notifyDataSetChanged()
}

    private inner class CrimeHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View?) {
//            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimeAdapter (var crimes: List<Crime>)
        : RecyclerView.Adapter<CrimeHolder>(){
//    private inner class CrimeAdapter(var crimes: List<Crime>)
//        : ListAdapter<Crime, CrimeHolder>(CrimeDiffCallback()){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            if(viewType == 0){      // 0 means does not require police
                val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
                return CrimeHolder(view)
            }
//            else if(viewType == 1){
            else{
                val view = layoutInflater.inflate(R.layout.list_item_crime_police, parent, false)
                return CrimeHolder(view)
            }
        }

        override fun getItemCount(): Int = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
//            holder.bind(getItem(position))
        }

        override fun getItemViewType(position: Int): Int {
            // Use requiresPolice var to determine which view to load on CrimeAdapter
            return crimes[position].requiresPolice
        }
    }

    private class CrimeDiffCallback : DiffUtil.ItemCallback<Crime>() {
            override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
                return oldItem.id == newItem.id
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}
