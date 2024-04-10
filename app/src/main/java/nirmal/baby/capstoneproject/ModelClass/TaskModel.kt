package nirmal.baby.capstoneproject.ModelClass

import android.os.Parcel
import android.os.Parcelable

class TaskModel (private var documentId: String, private var taskTitle: String, private var priorityType: String,
                        private var createdBy: String, private var taskDesc: String, private var taskAmt: String,
                            private var taskTip: String, private var taskDocs: String,
                                private var taskStatus: String, private var taskAcceptedBy: String,
                                    private var taskDue: String, private var lat: String,
                                        private var lon: String): Parcelable {

    // Getter and Setter
    fun getTaskStatus(): String {
        return taskStatus
    }

    fun getTaskAcceptedBy(): String {
        return taskAcceptedBy
    }
    fun getDocumentId(): String {
        return documentId
    }

    fun getTaskTitle(): String {
        return taskTitle
    }

    fun getPriorityType(): String {
        return priorityType
    }

    fun getUserName(): String {
        return createdBy
    }

    fun getTaskDesc(): String{
        return taskDesc
    }

    fun getTaskAmt(): String{
        return taskAmt
    }

    fun getTaskTip(): String{
        return taskTip
    }

    fun getTaskDocs(): String{
        return taskDocs
    }

    fun getTaskDue(): String{
        return taskDue
    }

    fun getLat(): String{
        return lat
    }

    fun getLon(): String{
        return lon
    }

    fun setTaskTitle(taskTitle: String) {
        this.taskTitle = taskTitle
    }

    fun setPriorityType(priorityType: String) {
        this.priorityType = priorityType
    }

    fun setUserName(userName: String) {
        this.createdBy = userName
    }

    // Parcelable implementation
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString()?:"",
        parcel.readString()?: "",
        "",
        "",
        ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(taskTitle)
        parcel.writeString(priorityType)
        parcel.writeString(createdBy)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskModel> {
        override fun createFromParcel(parcel: Parcel): TaskModel {
            return TaskModel(parcel)
        }

        override fun newArray(size: Int): Array<TaskModel?> {
            return arrayOfNulls(size)
        }
    }
}