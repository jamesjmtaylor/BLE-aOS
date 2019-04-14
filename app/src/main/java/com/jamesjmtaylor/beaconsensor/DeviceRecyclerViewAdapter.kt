package com.jamesjmtaylor.beaconsensor

import android.bluetooth.le.ScanResult
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import kotlinx.android.synthetic.main.row_result.view.*

class DeviceRecyclerViewAdapter(private val fragment: BluetoothSocketFragment,
                                private val listener: BluetoothSocketFragment.OnListFragmentInteractionListener?)
    : androidx.recyclerview.widget.RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder>() {
    //MARK: - Adapter methods
    private val results = mutableListOf<ScanResult>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.result = results.get(position)

        val item = holder.result ?: return
        holder.cellView.setOnClickListener {
            listener?.onListFragmentInteraction(item)
        }
    }

    override fun getItemCount(): Int {
        return results.size
    }

    fun updateAdapterWithNewList(newResults: List<ScanResult>?) {
        //DifUtil below keeps shifts in the new loaded list to a minimum
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return results.size
            }

            override fun getNewListSize(): Int {
                return newResults?.size ?: 0
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldId = results.get(oldItemPosition).device.address ?: return false
                val newId = newResults?.get(newItemPosition)?.device?.address ?: return false
                return oldId == newId
            }

            //Items may have the same id, but their contents may have been updated
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldResult = results.get(oldItemPosition)
                val newResult = newResults?.get(newItemPosition) ?: return false
                return oldResult.equals(newResult)
            }

            //This allows you to introspect on what exactly changed and report it to the adapter as a bundle
            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                return super.getChangePayload(oldItemPosition, newItemPosition)
            }
        })
        this.results.clear()
        newResults?.let { this.results.addAll(it) }
        diffResult.dispatchUpdatesTo(this)
    }

    //MARK: - ViewHolder class
    inner class ViewHolder(val cellView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(cellView) {
        var result: ScanResult? = null

        init {
            cellView.macTextView.text = "MAC: " + result?.device?.address
            cellView.nameTextView.text = "Name: " + result?.device?.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                cellView.txPowerTextView.text = "TX Power: " + result?.txPower
                cellView.rssiTextView.text = "RSSI: " + result?.txPower
            }
        }
    }
}
