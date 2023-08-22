package com.example.greenspace

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greenspace.ui.theme.GreenSpaceTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pusher.pushnotifications.PushNotifications
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PushNotifications.start(applicationContext, "82e5ff89-52a0-4850-98de-584f6e4e2774")
        PushNotifications.addDeviceInterest("hello")
        setContent {
            GreenSpaceTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyUI()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyUI() {
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val ecDatabaseReference = firebaseDatabase.getReference("ECValue")
    val waterLevelDatabaseReference = firebaseDatabase.getReference("WaterLevel")
    val nutrientLevelDatabaseReference = firebaseDatabase.getReference("NutrientLevel")
    val buttonState = firebaseDatabase.getReference("WaterPumpState")
    val drumSpeed = firebaseDatabase.getReference("DrumSpeed")

    val ecMsg = remember {
        mutableStateOf(0)
    }
    ecDatabaseReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val value = snapshot.getValue(Int::class.java)
            ecMsg.value = value!!
        }

        override fun onCancelled(error: DatabaseError) {
            ecMsg.value = 0
        }
    })

    val waterLevelMsg = remember {
        mutableStateOf(0)
    }
    waterLevelDatabaseReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val value = snapshot.getValue(Int::class.java)
            waterLevelMsg.value = value!!
        }

        override fun onCancelled(error: DatabaseError) {
            waterLevelMsg.value = 0
        }
    })

    val nutrientLevelMsg = remember {
        mutableStateOf(0)
    }
    nutrientLevelDatabaseReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val value = snapshot.getValue(Int::class.java)
            nutrientLevelMsg.value = value!!
        }

        override fun onCancelled(error: DatabaseError) {
            nutrientLevelMsg.value = 0
        }
    })

    var waterSwitchOn by remember {
        mutableStateOf(false)
    }

    var drumSwitchOn by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
    ) {
        Row(modifier = Modifier) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(260.dp)
                    .padding(20.dp)
                    .background(
                        color = Color.DarkGray,
                        shape = RoundedCornerShape(20.dp)
                    ),
            ) {
                Text(
                    text = "CONTROL",
                    color = Color.LightGray,
                    fontSize = 25.sp,
                    modifier = Modifier
                        .padding(top = 5.dp, start = 24.dp),
                    fontStyle = FontStyle.Italic
                )
                Divider(
                    color = Color.LightGray,
                    modifier = Modifier
                        .padding(top = 45.dp),
                    thickness = 2.dp
                )
                /* Water Control */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "Water Pump",
                        color = Color.LightGray,
                        fontSize = 19.sp
                    )
                    Switch(
                        modifier = Modifier
                            .scale(scale = 0.8f),
                        checked = waterSwitchOn,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Green,
                            checkedTrackColor = Color.LightGray,
                            checkedIconColor = Color.DarkGray,
                            uncheckedThumbColor = Color.Red,
                            uncheckedTrackColor = Color.LightGray,
                            uncheckedIconColor = Color.DarkGray
                        ),
                        thumbContent = {
                            Icon(
                                imageVector = if (waterSwitchOn) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        },
                        onCheckedChange = { switchOn_ ->
                            waterSwitchOn = switchOn_
                            if (waterSwitchOn) {
                                buttonState.setValue(1)
                            } else {
                                buttonState.setValue(0)
                            }
                        }
                    )
                }
                /* Drum Control */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 120.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "Rotary Drum",
                        color = Color.LightGray,
                        fontSize = 19.sp
                    )
                    Switch(
                        modifier = Modifier
                            .scale(scale = 0.8f),
                        checked = drumSwitchOn,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Green,
                            checkedTrackColor = Color.LightGray,
                            checkedIconColor = Color.DarkGray,
                            uncheckedThumbColor = Color.Red,
                            uncheckedTrackColor = Color.LightGray,
                            uncheckedIconColor = Color.DarkGray
                        ),
                        thumbContent = {
                            Icon(
                                imageVector = if (drumSwitchOn) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        },
                        onCheckedChange = { switchOn_ ->
                            drumSwitchOn = switchOn_
                            if (drumSwitchOn) {
                                drumSpeed.setValue(1)
                            } else {
                                drumSpeed.setValue(0)
                            }
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(260.dp)
                    .padding(20.dp)
                    .background(
                        color = Color.DarkGray,
                        shape = RoundedCornerShape(20.dp)
                    ),
            ) {
                Text(
                    text = "SENSOR",
                    color = Color.LightGray,
                    fontSize = 25.sp,
                    modifier = Modifier
                        .padding(top = 5.dp, start = 24.dp),
                    fontStyle = FontStyle.Italic
                )
                Divider(
                    color = Color.LightGray,
                    modifier = Modifier
                        .padding(top = 45.dp),
                    thickness = 2.dp
                )
                /* EC value */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 55.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ec_meter),
                        contentDescription = "EC Value",
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "EC",
                        color = Color.LightGray
                    )
                    Text(
                        text = ecMsg.value.toString(),
                        color = Color.LightGray
                    )
                }
                /* Water level value */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 105.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.water_level),
                        contentDescription = "Water level",
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Water",
                        color = Color.LightGray
                    )
                    if (waterLevelMsg.value == 0) {
                        Text(
                            text = "Low",
                            color = Color.LightGray
                        )
                    } else {
                        Text(
                            text = "High",
                            color = Color.LightGray
                        )
                    }
                }
                /* Nutrient level value */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 160.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.nutrient_level),
                        contentDescription = "Nutrient level",
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Nutrient",
                        color = Color.LightGray
                    )
                    if (nutrientLevelMsg.value == 0) {
                        Text(
                            text = "Low",
                            color = Color.LightGray
                        )
                    } else {
                        Text(
                            text = "High",
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp)
                .background(
                    color = Color.DarkGray,
                    shape = RoundedCornerShape(20.dp)
                )
        ){
            Text(
                text = "Select the date when you sowed your seeds",
                color = Color.LightGray,
                modifier = Modifier
                    .padding(top = 10.dp, start = 10.dp)
            )
            Row(
                modifier = Modifier
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Image(
                    painter = painterResource(id = R.drawable.germination),
                    contentDescription = "germination",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(top = 30.dp)
                )
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GerminationDate()
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 20.dp)
                .background(
                    color = Color.DarkGray,
                    shape = RoundedCornerShape(20.dp)
                )
        ){
            Text(
                text = "Select the date when you transferred the segmented trays",
                color = Color.LightGray,
                modifier = Modifier
                    .padding(top = 10.dp, start = 10.dp)
            )
            Row(
                modifier = Modifier
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Image(
                    painter = painterResource(id = R.drawable.harvest),
                    contentDescription = "harvest",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(top = 30.dp)
                )
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HarvestDate()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GerminationDate(){
    val mContext = LocalContext.current

    val mYear: Int
    val mMonth: Int
    val mDay: Int

    val mCalendar = Calendar.getInstance()

    mYear = mCalendar.get(Calendar.YEAR)
    mMonth = mCalendar.get(Calendar.MONTH)
    mDay = mCalendar.get(Calendar.DAY_OF_MONTH)

    mCalendar.time = Date()

    val newDate = remember {
        mutableStateOf("")
    }

    val mDatePickerDialog = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            data class DatePicker(val day: Int, val month: Int)
            val datePicker = DatePicker(mDayOfMonth, mMonth+1)
            val baseMonth = LocalDate.of(mYear, datePicker.month, datePicker.day)
            val nextMonth = LocalDate.from(baseMonth).plusDays(10)
            newDate.value = "$nextMonth"

        }, mYear, mMonth, mDay
    )

    Button(
        onClick = {
            mDatePickerDialog.show()
        },
        colors = ButtonDefaults.buttonColors(Color(0XFF0F9D58)),
        modifier = Modifier
    ) {
        Text(text = "Date", color = Color.White)
    }

    Text(text = "Transfer Date: ${newDate.value}", textAlign = TextAlign.Center, color = Color.LightGray)
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HarvestDate(){
    val mContext = LocalContext.current

    val mYear: Int
    val mMonth: Int
    val mDay: Int

    val mCalendar = Calendar.getInstance()

    mYear = mCalendar.get(Calendar.YEAR)
    mMonth = mCalendar.get(Calendar.MONTH)
    mDay = mCalendar.get(Calendar.DAY_OF_MONTH)

    mCalendar.time = Date()

    val newDate = remember {
        mutableStateOf("")
    }

    val mDatePickerDialog = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            data class DatePicker(val day: Int, val month: Int)
            val datePicker = DatePicker(mDayOfMonth, mMonth+1)
            val baseMonth = LocalDate.of(mYear, datePicker.month, datePicker.day)
            val nextMonth = LocalDate.from(baseMonth).plusDays(20)
            newDate.value = "$nextMonth"

        }, mYear, mMonth, mDay
    )

    Button(
        onClick = {
            mDatePickerDialog.show()
        },
        colors = ButtonDefaults.buttonColors(Color(0XFF0F9D58)),
        modifier = Modifier
    ) {
        Text(text = "Date", color = Color.White)
    }

    Text(text = "Harvest Date: ${newDate.value}", textAlign = TextAlign.Center, color = Color.LightGray)
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GreenSpaceTheme {
        MyUI()
    }
}