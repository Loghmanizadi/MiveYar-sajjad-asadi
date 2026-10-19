package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.EmployeeAttendanceEntity
import com.example.data.local.entities.EmployeeEntity
import com.example.data.local.entities.EmployeePaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY fullName ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id")
    fun getEmployeeById(id: Long): Flow<EmployeeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEmployees(employees: List<EmployeeEntity>)

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Delete
    suspend fun deleteEmployee(employee: EmployeeEntity)

    @Query("SELECT * FROM employee_attendance ORDER BY shamsiDate DESC")
    fun getAllAttendance(): Flow<List<EmployeeAttendanceEntity>>

    @Query("SELECT * FROM employee_attendance WHERE employeeId = :empId ORDER BY shamsiDate DESC")
    fun getAttendanceByEmployee(empId: Long): Flow<List<EmployeeAttendanceEntity>>

    @Query("SELECT * FROM employee_attendance WHERE shamsiDate = :date")
    fun getAttendanceByDate(date: String): Flow<List<EmployeeAttendanceEntity>>

    @Query("SELECT * FROM employee_attendance WHERE employeeId = :empId AND shamsiDate = :date LIMIT 1")
    suspend fun getAttendanceForEmployeeDate(empId: Long, date: String): EmployeeAttendanceEntity?

    @Query("SELECT * FROM employee_attendance WHERE shamsiDate BETWEEN :startDate AND :endDate")
    fun getAttendanceByDateRange(startDate: String, endDate: String): Flow<List<EmployeeAttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAttendance(attendance: EmployeeAttendanceEntity): Long

    @androidx.room.Update
    suspend fun updateAttendance(attendance: EmployeeAttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: EmployeeAttendanceEntity)

    @Query("SELECT * FROM employee_payments WHERE employeeId = :empId ORDER BY id DESC")
    fun getPaymentsByEmployee(empId: Long): Flow<List<EmployeePaymentEntity>>

    @Query("SELECT * FROM employee_payments ORDER BY id DESC")
    fun getAllPayments(): Flow<List<EmployeePaymentEntity>>

    @Query("SELECT * FROM employee_payments WHERE shamsiDate BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun getPaymentsByDateRange(startDate: String, endDate: String): Flow<List<EmployeePaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: EmployeePaymentEntity): Long

    @androidx.room.Update
    suspend fun updatePayment(payment: EmployeePaymentEntity)

    @Delete
    suspend fun deletePayment(payment: EmployeePaymentEntity)

    @Query("DELETE FROM employees")
    suspend fun clearAllEmployees()

    @Query("DELETE FROM employee_attendance")
    suspend fun clearAllAttendance()

    @Query("DELETE FROM employee_payments")
    suspend fun clearAllPayments()
}
