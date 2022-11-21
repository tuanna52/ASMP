'use strict';

// Set the setpoint in automatic mode
function setPointClicked(element){
  let current_room = document.getElementById("current-room").textContent;
  let tempauto_query = firebase.firestore().collection("automatic"+current_room[5]).doc("temperature");
  let humidauto_query = firebase.firestore().collection("automatic"+current_room[5]).doc("humidity");
  let co2auto_query = firebase.firestore().collection("automatic"+current_room[5]).doc("co2");
  switch (element.id) {
    case "temp-set-button":
      tempauto_query.update({"setpoint": parseFloat(setInputTemp.value)});
      break;
    case "humid-set-button":
      humidauto_query.update({"setpoint": parseFloat(setInputHumid.value)});
      break;
    case "co2-set-button":
      co2auto_query.update({"setpoint": parseFloat(setInputCO2.value)});
      break;
    default:
      break;
  }
}

// Actuator checkbox toggled
function actuatorToggle(element) {
  let current_room = document.getElementById("current-room").textContent;
  let tempman_query = firebase.firestore().collection("manual"+current_room[5]).doc("temperature");
  let humidman_query = firebase.firestore().collection("manual"+current_room[5]).doc("humidity");
  let co2man_query = firebase.firestore().collection("manual"+current_room[5]).doc("co2");

  if(element.checked) {
    element.parentElement.style.backgroundColor="#5d78ff";
    switch (element.id) {
      case "heater":
        tempman_query.update({"heater": 1});
        break;
      case "cooler":
        tempman_query.update({"cooler": 1});
        break;
      case "bumper":
        humidman_query.update({"bumper": 1});
        break;
      case "fan":
        co2man_query.update({"fan": 1});
        break;
      default:
        break;
    }
  } else {
    element.parentElement.style.backgroundColor="#e6e6e6";
    switch (element.id) {
      case "heater":
        tempman_query.update({"heater": 0});
        break;
      case "cooler":
        tempman_query.update({"cooler": 0});
        break;
      case "bumper":
        humidman_query.update({"bumper": 0});
        break;
      case "fan":
        co2man_query.update({"fan": 0});
        break;
      default:
        break;
    }
  }
}

//Update data in Monitoring section when changes happened
var LASTDAY = "";
function updateData(room_number) {
  let temp = document.getElementById("temp");
  let humid = document.getElementById("humid");
  let co2 = document.getElementById("co2");
  let timestamp = document.getElementsByClassName("timestamp");

  let query = firebase.firestore().collection("data" + room_number).orderBy('timestamp', 'desc').limit(1);
  // Start listening to the query.
  query.onSnapshot(function(snapshot) {
    snapshot.docChanges().forEach(function(change) {
      if (change.type === 'added') {
        let message = change.doc.data();
        temp.textContent = message.temperature;
        humid.textContent = message.humidity;
        co2.textContent = message.co2;
        LASTDAY = message.timestamp.substring(0, 10);
        for (let i = 0; i < timestamp.length; i++) {
          timestamp[i].innerHTML = message.timestamp;
        }
      }
    });
  });
}

// Update the status of Control Section
function updateControl(room_number) {
  let room_auto = "automatic" + room_number;
  let room_man = "manual" + room_number;
  let tempman_query = firebase.firestore().collection(room_man).doc("temperature");
  let humidman_query = firebase.firestore().collection(room_man).doc("humidity");
  let co2man_query = firebase.firestore().collection(room_man).doc("co2");

  let tempauto_query = firebase.firestore().collection(room_auto).doc("temperature");
  let humidauto_query = firebase.firestore().collection(room_auto).doc("humidity");
  let co2auto_query = firebase.firestore().collection(room_auto).doc("co2");

  tempauto_query.onSnapshot(function(doc) {
    var source = doc.metadata.hasPendingWrites ? 0 : 1;   //Change from server: 1 - Change from local: 0
    if (source) {
      autoswitchTemp.checked = doc.data().status;
      autoTemp();
    }
  });

  tempman_query.onSnapshot(function(doc) {
    var source = doc.metadata.hasPendingWrites ? 0 : 1;   //Change from server: 1 - Change from local: 0
    if (source) {
      if(doc.data().status) {
        manswitchTemp.checked = true;
        manTemp();
        heater.checked = doc.data().heater;
        actuatorToggle(heater);
        cooler.checked = doc.data().cooler;
        actuatorToggle(cooler);
      } else {
        manswitchTemp.checked = false;
        manTemp();
      }
    }
  });

  humidauto_query.onSnapshot(function(doc) {
    var source = doc.metadata.hasPendingWrites ? 0 : 1;   //Change from server: 1 - Change from local: 0
    if (source) {
      autoswitchHumid.checked = doc.data().status;
      autoHumid();
    }
  });

  humidman_query.onSnapshot(function(doc) {
    var source = doc.metadata.hasPendingWrites ? 0 : 1;   //Change from server: 1 - Change from local: 0
    if (source) {
      if(doc.data().status) {
        manswitchHumid.checked = true;
        manHumid();
        bumper.checked = doc.data().bumper;
        actuatorToggle(bumper);
      } else {
        manswitchHumid.checked = false;
        manHumid();
      }
    }
  });

  co2auto_query.onSnapshot(function(doc) {
    var source = doc.metadata.hasPendingWrites ? 0 : 1;   //Change from server: 1 - Change from local: 0
    if (source) {
      autoswitchCO2.checked = doc.data().status;
      autoCO2();
    }
  });

  co2man_query.onSnapshot(function(doc) {
    var source = doc.metadata.hasPendingWrites ? 0 : 1;   //Change from server: 1 - Change from local: 0
    if (source) {
      if(doc.data().status) {
        manswitchCO2.checked = true;
        manCO2();
        fan.checked = doc.data().fan;
        actuatorToggle(fan);
      } else {
        manswitchCO2.checked = false;
        manCO2();
      }
    }
  });
}

// Temperature automatic-manual switch is toggled
var autoswitchTemp = document.getElementById("temp-auto");
var manswitchTemp = document.getElementById("temp-man");
var setInputTemp = document.getElementById("temp-setpoint");
var setButtonTemp = document.getElementById("temp-set-button");
var heater = document.getElementById("heater");
var cooler = document.getElementById("cooler");
// Automatic switch
function autoTemp() {
  let current_room = document.getElementById("current-room").textContent;
  let tempman_query = firebase.firestore().collection("manual"+current_room[5]).doc("temperature");
  let tempauto_query = firebase.firestore().collection("automatic"+current_room[5]).doc("temperature");

  if (autoswitchTemp.checked == true) {
    setInputTemp.disabled = false;
    setButtonTemp.disabled = false;
    setInputTemp.style.background = "inherit";
    tempauto_query.get().then(function(doc) {
      setInputTemp.value = doc.data().setpoint;
    });
    manswitchTemp.checked = false;
    manswitchTemp.onchange();
    heater.disabled = true;
    cooler.disabled = true;
    tempauto_query.update({'status': 1});
    tempman_query.update({'status': 0});
  } else {
    setInputTemp.disabled = true;
    setButtonTemp.disabled = true;
    setInputTemp.value = "";
    setInputTemp.style.background = "#e6e6e6";
    tempauto_query.update({'status': 0});
  }
}
// Manual switch
function manTemp() {
  let current_room = document.getElementById("current-room").textContent;
  let tempman_query = firebase.firestore().collection("manual"+current_room[5]).doc("temperature");
  let tempauto_query = firebase.firestore().collection("automatic"+current_room[5]).doc("temperature");
  if (manswitchTemp.checked == true) {
    heater.disabled = false;
    cooler.disabled = false;
    autoswitchTemp.checked = false;
    autoswitchTemp.onchange();
    tempman_query.update({'status': 1});
    tempauto_query.update({'status': 0});
  } else {
    heater.disabled = true;
    cooler.disabled = true;
    heater.checked = false;
    actuatorToggle(heater);
    cooler.checked = false;
    actuatorToggle(cooler);
    tempman_query.update({'status': 0});
  }
}

// Humidity automatic-manual switch is toggled
var autoswitchHumid = document.getElementById("humid-auto");
var manswitchHumid = document.getElementById("humid-man");
var setInputHumid = document.getElementById("humid-setpoint");
var setButtonHumid = document.getElementById("humid-set-button");
var bumper = document.getElementById("bumper");
// Automatic switch
function autoHumid() {
  let current_room = document.getElementById("current-room").textContent;
  let humidman_query = firebase.firestore().collection("manual"+current_room[5]).doc("humidity");
  let humidauto_query = firebase.firestore().collection("automatic"+current_room[5]).doc("humidity");
  if (autoswitchHumid.checked == true) {
    setInputHumid.disabled = false;
    setButtonHumid.disabled = false;
    setInputHumid.style.background = "inherit";
    humidauto_query.get().then(function(doc) {
      setInputHumid.value = doc.data().setpoint;
    });
    manswitchHumid.checked = false;
    manswitchHumid.onchange();
    bumper.disabled = true;
    humidauto_query.update({'status': 1});
    humidman_query.update({'status': 0});
  } else {
    setInputHumid.disabled = true;
    setButtonHumid.disabled = true;
    setInputHumid.value = "";
    setInputHumid.style.background = "#e6e6e6";
    humidauto_query.update({'status': 0});
  }
}
// Manual switch
function manHumid() {
  let current_room = document.getElementById("current-room").textContent;
  let humidman_query = firebase.firestore().collection("manual"+current_room[5]).doc("humidity");
  let humidauto_query = firebase.firestore().collection("automatic"+current_room[5]).doc("humidity");
  if (manswitchHumid.checked == true) {
    bumper.disabled = false;
    autoswitchHumid.checked = false;
    autoswitchHumid.onchange();
    humidman_query.update({'status': 1});
    humidauto_query.update({'status': 0});
  } else {
    bumper.disabled = true;
    bumper.checked = false;
    actuatorToggle(bumper);
    humidman_query.update({'status': 0});
  }
}

// CO2 automatic-manual switch is toggled
var autoswitchCO2 = document.getElementById("co2-auto");
var manswitchCO2 = document.getElementById("co2-man");
var setInputCO2 = document.getElementById("co2-setpoint");
var setButtonCO2 = document.getElementById("co2-set-button");
var fan = document.getElementById("fan");
// Automatic switch
function autoCO2() {
  let current_room = document.getElementById("current-room").textContent;
  let co2man_query = firebase.firestore().collection("manual"+current_room[5]).doc("co2");
  let co2auto_query = firebase.firestore().collection("automatic"+current_room[5]).doc("co2");
  if (autoswitchCO2.checked == true) {
    setInputCO2.disabled = false;
    setButtonCO2.disabled = false;
    setInputCO2.style.background = "inherit";
    co2auto_query.get().then(function(doc) {
      setInputCO2.value = doc.data().setpoint;
    });
    manswitchCO2.checked = false;
    manswitchCO2.onchange();
    fan.disabled = true;
    co2auto_query.update({'status': 1});
    co2man_query.update({'status': 0});
  } else {
    setInputCO2.disabled = true;
    setButtonCO2.disabled = true;
    setInputCO2.value = "";
    setInputCO2.style.background = "#e6e6e6";
    co2auto_query.update({'status': 0});
  }
}
// Manual switch
function manCO2() {
  let current_room = document.getElementById("current-room").textContent;
  let co2man_query = firebase.firestore().collection("manual"+current_room[5]).doc("co2");
  let co2auto_query = firebase.firestore().collection("automatic"+current_room[5]).doc("co2");
  if (manswitchCO2.checked == true) {
    fan.disabled = false;
    autoswitchCO2.checked = false;
    autoswitchCO2.onchange();
    co2man_query.update({'status': 1});
    co2auto_query.update({'status': 0});
  } else {
    fan.disabled = true;
    fan.checked = false;
    actuatorToggle(fan);
    co2man_query.update({'status': 0});
  }
}

// Show actived room in side tab and main header
function selectRoom(evt, room) {
    let tablinks, tabname, bullet;
    tablinks = document.getElementsByClassName("tablinks");
    bullet = document.getElementsByClassName("circle-bullet");
    for (let i = 0; i < tablinks.length; i++) {
      tablinks[i].className = tablinks[i].className.replace(" active", "");
      bullet[i].style.color = "#989eb3";
    }
    tabname = document.getElementById("current-room");

    // Show the current tab, and add an "active" class to the link that opened the tab
    evt.currentTarget.className += " active";
    evt.currentTarget.children[0].style.color = "#5867dd";
    tabname.textContent = room;
    updateData(room[5]);
    document.getElementById("day-observation").click();
    updateControl(room[5]);
}
document.getElementById("defaultOpen").click();

// Show data by day, month, or year in graph
function selectTypeOfObs(evt, chosentype) {
  let alltypes = document.getElementsByClassName('day-month-year');
  let currentroom = document.getElementById("current-room").textContent;
  let tempdata = [], humiddata = [], co2data = [], timestamp = [];

  for(let i = 0; i < alltypes.length; i++) {
    alltypes[i].style.color = "#646c9a";
    alltypes[i].style.backgroundColor = "#f0f0f0";
  }

  chosentype.style.color = "#5867dd";
  chosentype.style.backgroundColor = "#f0f3ff";

  firebase.firestore().collection("data"+currentroom[5]).orderBy('timestamp', 'desc').get().then(function(querySnapshot) {
    querySnapshot.forEach(function(doc) {
      tempdata.push(doc.data().temperature);
      humiddata.push(doc.data().humidity);
      co2data.push(doc.data().co2);
      timestamp.push(doc.data().timestamp);
    });

    if (chosentype.id == "day-observation") {
      let dataday = 0;    // the number of data document of lastest day
      let lastdate = timestamp[0].substring(0, 10);
      chart.data.labels.length = 0;
      chart.data.datasets[0].data.length = 0;
      chart.data.datasets[1].data.length = 0;
      chart.data.datasets[2].data.length = 0;
      while (dataday < timestamp.length && timestamp[dataday].substring(0, 10) == lastdate) dataday++;
      for (let i = dataday-1; i >= 0; i--) {
        chart.data.labels.push(timestamp[i].substring(13));
        chart.data.datasets[0].data.push(tempdata[i]);
        chart.data.datasets[1].data.push(humiddata[i]);
        chart.data.datasets[2].data.push(co2data[i]);
        chart.update();
      }
    }
    else if (chosentype.id == "month-observation") {
      let datamonth = 0;  // the number of data document of the latest month
      let lastmonth = timestamp[0].substring(0,7);
      chart.data.labels.length = 0;
      chart.data.datasets[0].data.length = 0;
      chart.data.datasets[1].data.length = 0;
      chart.data.datasets[2].data.length = 0;
      while (datamonth < timestamp.length && timestamp[datamonth].substring(0, 7) == lastmonth) datamonth++;
      for (let i = datamonth-1; i >= 0; i--) {
        chart.data.labels.push(timestamp[i].substring(5,10));
        chart.data.datasets[0].data.push(tempdata[i]);
        chart.data.datasets[1].data.push(humiddata[i]);
        chart.data.datasets[2].data.push(co2data[i]);
        chart.update();
      }
    }
    else {
      let datayear = 0;   // the number of data document of the latest year
      let lastyear = timestamp[0].substring(0, 4);
      chart.data.labels.length = 0;
      chart.data.datasets[0].data.length = 0;
      chart.data.datasets[1].data.length = 0;
      chart.data.datasets[2].data.length = 0;
      while (datayear < timestamp.length && timestamp[datayear].substring(0, 4) == lastyear) datayear++;
      for (let i = datayear-1; i >= 0; i--) {
        chart.data.labels.push(timestamp[i].substring(0,7));
        chart.data.datasets[0].data.push(tempdata[i]);
        chart.data.datasets[1].data.push(humiddata[i]);
        chart.data.datasets[2].data.push(co2data[i]);
        chart.update();
      }
    }
  });

}

// Calculate average data on the last day
function calAvr() {
  let currentroom = document.getElementById("current-room").textContent;
  let avrtemp = document.getElementById("avr-temp");
  let avrhumid = document.getElementById("avr-humid");
  let avrco2 = document.getElementById("avr-co2");
  let tempdata = [], humiddata = [], co2data = [];
  let tresult = 0, hresult = 0, cresult = 0;

  firebase.firestore().collection("data"+currentroom[5]).where("timestamp", ">", LASTDAY + " | 00:00:00").get().then(function(querySnapshot) {
    querySnapshot.forEach(function(doc) {
      tempdata.push(doc.data().temperature);
      humiddata.push(doc.data().humidity);
      co2data.push(doc.data().co2);
    });
    for(let i = 0; i < tempdata.length; i++) {
      tresult += tempdata[i];
      hresult += humiddata[i];
      cresult += co2data[i];
    }
    avrtemp.textContent = (tresult/tempdata.length).toFixed(1);
    avrhumid.textContent = (hresult/tempdata.length).toFixed(2);
    avrco2.textContent = (cresult/tempdata.length).toFixed(0);
  });

}

// Show status of notification bell
// window.addEventListener('click', function(e){   
//   if (document.getElementById("noti-bell").contains(e.target)){
//     let bell = document.getElementsByClassName("notification-bell");
//     if (bell[0].style.backgroundColor == "rgb(240, 243, 255)" && bell[0].style.color == "rgb(93, 120, 255)") {
//       bell[0].style.color = "#646c9a";
//       bell[0].style.backgroundColor = null;
//     } else {
//       bell[0].style.color = "#5d78ff";
//       bell[0].style.backgroundColor = "#f0f3ff";
//     }
//   } else {
//     let bell = document.getElementsByClassName("notification-bell");
//     bell[0].style.color = "#646c9a";
//     bell[0].style.backgroundColor = null;
//   }
// });

// Draw graph
// function drawChart(tempdata, humiddata, co2data, xlabels) {
//   // Dataset

//   chart.update();
// }

var ctx = document.getElementById('chart-3').getContext('2d');
var chart = new Chart(ctx, {
  // The type of chart
  type: 'line',
  // Data of chart
  data: {
    labels: [],
    datasets: [{
        label: 'Temperature',
        borderColor: '#fa7ba5',
        data: [],
        fill: false,
    },  {
        label: 'Humidity',
        borderColor: '#21ecec',
        data: [],
        fill: false,
    },  {
        label: 'CO2',
        borderColor: '#21ec65',
        data: [],
        fill: false,
    }]
  },
  // Configuration options
  options: {
    scales: {
      xAxes: [{
          gridLines: {
              borderDash: [8, 4],
          }
      }],

      yAxes: [{
          gridLines: {
              borderDash: [8, 4],
          }
      }]
    },
    legend: {
      align: 'end',
      labels: {
        padding: 10
      }
    }
  },

  plugins: [{
    beforeInit: function(chart) {
      chart.legend.afterFit = function() {
        this.height = this.height + 10;
      };
    }
  }]
});