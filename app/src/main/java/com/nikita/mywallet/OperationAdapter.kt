package com.nikita.mywallet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class OperationAdapter(
    context: Context,
    resource: Int,
    objects: List<Operation>,
    private val selectedOperationId: Int?
) : ArrayAdapter<Operation>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val operation = getItem(position)
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.operation_item, parent, false)

        val itemId = view.findViewById<TextView>(R.id.item_id)
        val itemAmount = view.findViewById<TextView>(R.id.item_amount)
        val itemType = view.findViewById<TextView>(R.id.item_type)
        val itemDescription = view.findViewById<TextView>(R.id.item_description)
        val itemDate = view.findViewById<TextView>(R.id.item_date)

        val amount = operation?.amount ?: 0.0
        val type = operation?.type.orEmpty()
        val sign = if (type == context.getString(R.string.expense)) "-" else "+"

        itemId.text = context.getString(R.string.operation_number, operation?.id ?: 0)
        itemAmount.text = context.getString(R.string.operation_amount, sign, amount)
        itemType.text = context.getString(R.string.operation_type, type)
        itemDescription.text = context.getString(R.string.operation_description, operation?.description.orEmpty())
        itemDate.text = context.getString(R.string.operation_date, operation?.date.orEmpty())

        val background = if (operation?.id == selectedOperationId) {
            R.drawable.selected_item_background
        } else {
            R.drawable.item_background
        }
        view.setBackgroundResource(background)

        return view
    }
}
