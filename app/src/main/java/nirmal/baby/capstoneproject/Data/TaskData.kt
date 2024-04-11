package nirmal.baby.capstoneproject.Data

data class TaskData(val title: String,
                    val description: String,
                    val amount: String,
                    val tip: String,
                    val priority: String,
                    val docs: String,
                    val dateDue: String,
                    val timeDue: String,
                    val createdBy: String,
                    val status: String,
                    val acceptedBy: String,
                    val latitude: String,
                    val longitude: String,
                    val ratings: String){
    // Empty constructor required for Firestore deserialization
    constructor() : this("", "", "", "", "", "", "", "", "","","","","","")
}

