package com.nikita.mywallet

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var mainType: Spinner
    private var selectedOperationId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        mainType = findViewById(R.id.main_type)

        setupSpinner()
        setupButtons()
        setupOperationList()
        updateOperationList()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.addButton).setOnClickListener {
            addOperation()
        }

        findViewById<Button>(R.id.deleteButton).setOnClickListener {
            deleteOperation()
        }

        findViewById<Button>(R.id.updateButton).setOnClickListener {
            updateOperation()
        }
    }

    private fun setupOperationList() {
        val mainOperationList = findViewById<ListView>(R.id.main_operationList)
        mainOperationList.setOnItemClickListener { _, _, position, _ ->
            val operation = dbHelper.getAllOperations()[position]
            selectedOperationId = operation.id

            findViewById<EditText>(R.id.main_amount).setText(formatAmount(operation.amount))
            findViewById<EditText>(R.id.main_description).setText(operation.description)
            mainType.setSelection(if (operation.type == getString(R.string.expense)) 1 else 0)
            updateSelectedOperationLabel()
            updateOperationList()
        }
    }

    private fun updateBalance() {
        val mainBalance = findViewById<TextView>(R.id.main_balance)
        val balance = dbHelper.getBalance()
        mainBalance.text = getString(R.string.total_balance, balance)
    }

    private fun setupSpinner() {
        val types = arrayOf(getString(R.string.income), getString(R.string.expense))
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mainType.adapter = adapter
    }

    private fun addOperation() {
        val mainAmount = findViewById<EditText>(R.id.main_amount)
        val mainDescription = findViewById<EditText>(R.id.main_description)

        val amount = mainAmount.text.toString().toDoubleOrNull()
        val type = mainType.selectedItem.toString()
        val description = mainDescription.text.toString().trim()
        val date = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())

        if (amount == null || amount <= 0 || description.isBlank()) {
            Toast.makeText(this, R.string.fill_fields, Toast.LENGTH_SHORT).show()
            return
        }

        dbHelper.insertOperation(amount, type, description, date)
        Toast.makeText(this, R.string.operation_added, Toast.LENGTH_SHORT).show()
        clearForm()
        updateOperationList()
    }

    private fun deleteOperation() {
        val id = selectedOperationId
        if (id == null) {
            Toast.makeText(this, R.string.select_operation_first, Toast.LENGTH_SHORT).show()
            return
        }

        val deletedRows = dbHelper.deleteOperation(id)
        if (deletedRows > 0) {
            Toast.makeText(this, R.string.operation_deleted, Toast.LENGTH_SHORT).show()
            clearForm()
            updateOperationList()
        } else {
            Toast.makeText(this, R.string.operation_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateOperation() {
        val id = selectedOperationId
        val mainAmount = findViewById<EditText>(R.id.main_amount)
        val mainDescription = findViewById<EditText>(R.id.main_description)

        val amount = mainAmount.text.toString().toDoubleOrNull()
        val type = mainType.selectedItem.toString()
        val description = mainDescription.text.toString().trim()
        val date = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())

        if (id == null) {
            Toast.makeText(this, R.string.select_operation_first, Toast.LENGTH_SHORT).show()
            return
        }

        if (amount == null || amount <= 0 || description.isBlank()) {
            Toast.makeText(this, R.string.fill_fields, Toast.LENGTH_SHORT).show()
            return
        }

        val updatedRows = dbHelper.updateOperation(id, amount, type, description, date)
        if (updatedRows > 0) {
            Toast.makeText(this, R.string.operation_updated, Toast.LENGTH_SHORT).show()
            clearForm()
            updateOperationList()
        } else {
            Toast.makeText(this, R.string.operation_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateOperationList() {
        val operations = dbHelper.getAllOperations()
        val operationAdapter = OperationAdapter(
            this,
            R.layout.operation_item,
            operations,
            selectedOperationId
        )
        val mainOperationList = findViewById<ListView>(R.id.main_operationList)
        mainOperationList.adapter = operationAdapter
        updateBalance()
        updateSelectedOperationLabel()
    }

    private fun updateSelectedOperationLabel() {
        val selectedOperationLabel = findViewById<TextView>(R.id.selected_operation)
        selectedOperationLabel.text = if (selectedOperationId != null) {
            getString(R.string.selected_operation, selectedOperationId)
        } else {
            getString(R.string.no_selected_operation)
        }
    }

    private fun clearForm() {
        selectedOperationId = null
        findViewById<EditText>(R.id.main_amount).text.clear()
        findViewById<EditText>(R.id.main_description).text.clear()
        mainType.setSelection(0)
        updateSelectedOperationLabel()
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            amount.toInt().toString()
        } else {
            amount.toString()
        }
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }
}
