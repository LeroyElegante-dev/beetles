package com.example.beetles.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.beetles.R
import com.example.beetles.db.ScoreEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordsAdapter(
    private val records: List<ScoreEntity>
) : BaseAdapter() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun getCount(): Int = records.size

    override fun getItem(position: Int): ScoreEntity = records[position]

    override fun getItemId(position: Int): Long = records[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        val record = records[position]
        view.findViewById<TextView>(R.id.tvRecordName).text = record.playerName
        view.findViewById<TextView>(R.id.tvRecordScore).text = parent.context.getString(
            R.string.record_line,
            record.score,
            record.difficulty,
            record.accuracy
        )
        view.findViewById<TextView>(R.id.tvRecordDetails).text = parent.context.getString(
            R.string.record_details,
            record.hits,
            record.misses,
            dateFormat.format(Date(record.createdAt))
        )
        return view
    }
}
