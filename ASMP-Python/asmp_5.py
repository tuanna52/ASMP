from guizero import App, Box, Picture, Text, Drawing, PushButton, CheckBox, TextBox
import threading
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
from time import strftime, sleep
from ast import literal_eval
import serial

class Main_App(object):
    def __init__(self, master):
        self.ASMP = master
        self.ASMP.set_full_screen()
        #Header
        self.Header = Box (master, height = 80, width = "fill", border=None, align="top")
        self.Header.bg = "#242939"
        Text (self.Header, width=3, align="left")            #This is used to place elements properly
        Picture (self.Header, image = "mushroom.png", align = "left")
        Text (self.Header, text="Automation System for Mushroom Production", color="#5867dd", font="Arial Bold", size=20, height=80, align="left")

        #Footer
        self.Footer = Box (master, height=40, width="fill", border=None, align="bottom")
        self.Footer.bg = "#e8e9ee"
        Text (self.Footer, width=16, align="left")           #This is used to place elements properly
        Picture (self.Footer, height=30, width=40, image = "uet.png", align="left")
        Text (self.Footer, height=50, text = "VNU - University of Engineering and Technology", font="Arial", size = 14, align="left")
        Picture (self.Footer, height=30, width=40, image = "vnu.png", align="left")
        Text (self.Footer, width=25, align="left")           #This is used to place elements properly

        #Side Tab
        self.Side_Tab = Box (master, height="fill", width=125, align="left")
        self.Side_Tab.bg = "#f0f3ff"
        Text (self.Side_Tab, height=4, align="top")           #This is used to place elements properly
        self.Dash_Button = PushButton (self.Side_Tab, text="Dashboard", pady=5, padx=3, align="top", command=self.dashboard_click)
        self.Dash_Button.text_color = "#ffffff"
        self.Dash_Button.text_size = 12
        self.Dash_Button.font = "Arial Bold"
        self.Dash_Button.bg = "#2d3244"
        self.Dash_Button.tk.config(relief="flat")
        Text (self.Side_Tab, height=2, align="top")           #This is used to place elements properly
        self.Control_Button = PushButton (self.Side_Tab, text="Control", pady=5, padx=17, align="top", command=self.control_click)
        self.Control_Button.text_color = "#ffffff"
        self.Control_Button.text_size = 12
        self.Control_Button.font = "Arial Bold"
        self.Control_Button.bg = "#2d3244"
        self.Control_Button.tk.config(relief="flat")
        Text (self.Side_Tab, height=2, align="top")           #This is used to place elements properly
        self.Control_Button = PushButton (self.Side_Tab, text="Exit", pady=5, padx=31, align="top", command=self.exit_click)
        self.Control_Button.text_color = "#ffffff"
        self.Control_Button.text_size = 12
        self.Control_Button.font = "Arial Bold"
        self.Control_Button.bg = "#2d3244"
        self.Control_Button.tk.config(relief="flat")

        #Dashboard page
        self.Dashboard = Box (master, height="fill", width="fill", layout="grid", visible=True)
        Box (self.Dashboard, height=84, width=40, grid=[0, 0])    #This is used to place element properly

        self.Temp_Box = Box (self.Dashboard, height=200, width=170, grid=[1, 1])
        self.Temp_Box.bg = "#f9a2a2"
        Text (self.Temp_Box, text="Temperature (°C)", size=15, color="#303b6a", font="Arial", align="top").tk.config(pady=4)
        self.Temp_Value = Text (self.Temp_Box, height=2, text="0", size=37, color="#303b6a", font="Arial Bold", align="top")
        Picture (self.Temp_Box, image="temperature.png", align="top")

        Box (self.Dashboard, width=42, grid=[2, 0])              #This is used to place element properly

        self.Humid_Box = Box (self.Dashboard, height=200, width=170, grid=[3, 1])
        self.Humid_Box.bg = "#90e9eb"
        Text (self.Humid_Box, text="Humidity (%)", size=15, color="#303b6a", font="Arial", align="top").tk.config(pady=4)
        self.Humid_Value = Text (self.Humid_Box, height=2, text="0", size=37, color="#303b6a", font="Arial Bold", align="top")
        Picture (self.Humid_Box, image="humidity.png", align="top")

        Box (self.Dashboard, width=42, grid=[4, 0])              #This is used to place element properly

        self.CO2_Box = Box (self.Dashboard, height=200, width=170, grid=[5, 1])
        self.CO2_Box.bg = "#96f4b8"
        Text (self.CO2_Box, text="CO2 (ppm)", size=15, color="#303b6a", font="Arial", align="top").tk.config(pady=4)
        self.CO2_Value = Text (self.CO2_Box, height=2, text="0", size=37, color="#303b6a", font="Arial Bold", align="top")
        Picture (self.CO2_Box, image="co2.png", align="top")

        #Control page
        self.Control = Box (master, height="fill", width="fill", layout="grid", visible=False)
        Box (self.Control, height=84, width=40, grid=[0, 0])         #This is used to place element properly

        self.Temp_Ctr = Box(self.Control, height=200, width=170, grid=[1, 1])
        self.Temp_Ctr.bg = "#e3e5ee"
        Text (self.Temp_Ctr, text="Temperature Control", size=13, color="#303b6a", font="Arial Bold", align="top").tk.config(pady=12)
        self.Temp_Heater = CheckBox(self.Temp_Ctr, text="Heater", align="top", height=3, command=self.heater_toggle)
        self.Temp_Heater.tk.config(fg = "#303b6a", font="Arial 13")
        self.Temp_Cooler = CheckBox(self.Temp_Ctr, text="Cooler", align="top", height=2, command=self.cooler_toggle)
        self.Temp_Cooler.tk.config(fg = "#303b6a", font="Arial 13")


        Box (self.Control, width=42, grid=[2, 0])                #This is used to place element properly

        self.Humid_Ctr = Box(self.Control, height=200, width=170, grid=[3, 1])
        self.Humid_Ctr.bg = "#e3e5ee"
        Text (self.Humid_Ctr, text="Humidity Control", size=13, color="#303b6a", font="Arial Bold", align="top").tk.config(pady=12)
        self.Humid_Bumper = CheckBox(self.Humid_Ctr, text="Pump", align="top", height=6, command=self.bumper_toggle)
        self.Humid_Bumper.tk.config(fg = "#303b6a", font="Arial 13")

        Box (self.Control, width=42, grid=[4, 0])                #This is used to place element properly

        self.CO2_Ctr = Box(self.Control, height=200, width=170, grid=[5, 1])
        self.CO2_Ctr.bg = "#e3e5ee"
        Text (self.CO2_Ctr, text="CO2 Control", size=13, color="#303b6a", font="Arial Bold", align="top").tk.config(pady=12)
        self.CO2_Fan = CheckBox(self.CO2_Ctr, text="Fan", align="top", height=6, command=self.fan_toggle)
        self.CO2_Fan.tk.config(fg = "#303b6a", font="Arial 13")
    #####################################################################################################################
    def dashboard_click(self):
        self.Control.visible = False
        self.Dashboard.visible = True

    def control_click(self):
        self.Dashboard.visible = False
        self.Control.visible = True

    def exit_click(self):
        self.ASMP.destroy()

    def heater_toggle(self):
        tempauto_ref.update({u'status': 0})
        if self.Temp_Heater.value == 1:
            tempman_ref.update({
            u'status': 1,
            u'heater': 1
            })
        else:
            tempman_ref.update({
            u'heater': 0
            })
    
    def cooler_toggle(self):
        tempauto_ref.update({u'status': 0})
        if self.Temp_Cooler.value == 1:
            tempman_ref.update({
            u'status': 1,
            u'cooler': 1
            })
        else:
            tempman_ref.update({
            u'cooler': 0
            })

    def bumper_toggle(self):
        humidauto_ref.update({u'status': 0})
        if self.Humid_Bumper.value == 1:
            humidman_ref.update({
            u'status': 1,
            u'bumper': 1
            })
        else:
            humidman_ref.update({
            u'bumper': 0
            })

    def fan_toggle(self):
        co2auto_ref.update({u'status': 0})
        if self.CO2_Fan.value == 1:
            co2man_ref.update({
            u'status': 1,
            u'fan': 1
            })
        else:
            co2man_ref.update({
            u'fan': 0
            })

    def update_value(self):
        self.Temp_Value.value = Temp
        self.Humid_Value.value = Humid
        self.CO2_Value.value = CO2
        self.ASMP.update()

    # Create a callback on_snapshot function to capture changes
    def on_snapshot(self, doc_snapshot, changes, read_time):
        for doc in doc_snapshot:
            doc_data = doc.to_dict()
            if doc_data['id'] == 1:
                if doc_data['status'] == 1:
                    self.Temp_Heater.value = doc_data['heater']
                    self.Temp_Cooler.value = doc_data['cooler']
            elif doc_data['id'] == 2:
                if doc_data['status'] == 1:
                    self.Humid_Bumper.value = doc_data['bumper']
            elif doc_data['id'] == 3:
                if doc_data['status'] == 1:
                    self.CO2_Fan.value = doc_data['fan']

    def update_actuator(self):
        self.tempman_watch = tempman_ref.on_snapshot(self.on_snapshot)
        self.humidman_watch = humidman_ref.on_snapshot(self.on_snapshot)
        self.co2man_watch = co2man_ref.on_snapshot(self.on_snapshot)
############################################################################################################

# Create a callback on_snapshot function to capture changes
def on_snapshot(doc_snapshot, changes, read_time):
    for doc in doc_snapshot:
        doc_data = doc.to_dict()
        message = (str(doc_data) + '\n').encode()
        # if doc_data['status'] == 1:
        #     ser.write(message)
        ser.write(message)

def bg_process():
    global Temp
    global Humid
    global CO2

    tempman_watch = tempman_ref.on_snapshot(on_snapshot)
    humidman_watch = humidman_ref.on_snapshot(on_snapshot)
    co2man_watch = co2man_ref.on_snapshot(on_snapshot)

    tempauto_watch = tempauto_ref.on_snapshot(on_snapshot)
    humidauto_watch = humidauto_ref.on_snapshot(on_snapshot)
    co2auto_watch = co2auto_ref.on_snapshot(on_snapshot)

    while not finish:
        try:
            if ser.inWaiting() > 0:
                predata = ser.readline().decode("utf-8")
                print(predata)
                data = literal_eval(predata)
                
                CO2 = data['CO2']
                Humid = data['Humidity']
                Temp = data['Temperature']

                # db.collection(u'data1').add({
                #     u'temperature': Temp,
                #     u'humidity': Humid,
                #     u'co2': CO2,
                #     u'timestamp': strftime("%Y-%m-%d") + " | " + strftime("%H:%M:%S"),
                # })
        except:
            print("Something is wrong!!!")
            sleep(1)

    # Terminate watch on a document
    tempman_watch.unsubscribe()
    humidman_watch.unsubscribe()
    co2man_watch.unsubscribe()
    tempauto_watch.unsubscribe()
    humidauto_watch.unsubscribe()
    co2auto_watch.unsubscribe()

#############################################################################################################
Temp = 0
Humid = 0
CO2 = 0

#Serial port
serial_port = 'COM4'
#Connect to Serial Port for communication
while 1:
    try:
        ser = serial.Serial(serial_port, 115200, timeout=0.2, writeTimeout=0.2)
        break
    except:
        print('Cannot open COM port')
        sleep(5)

# Use firestore service account
cred = credentials.Certificate('asmp-uet-firebase-adminsdk-4nk8w-738f0a3245.json')
firebase_admin.initialize_app(cred)
db = firestore.client()

tempman_ref = db.collection(u'manual1').document(u'temperature')
humidman_ref = db.collection(u'manual1').document(u'humidity')
co2man_ref = db.collection(u'manual1').document(u'co2')

tempauto_ref = db.collection(u'automatic1').document(u'temperature')
humidauto_ref = db.collection(u'automatic1').document(u'humidity')
co2auto_ref = db.collection(u'automatic1').document(u'co2')

finish = False
backGroundProcess = threading.Thread(target=bg_process)
backGroundProcess.start()

#Create user interface
mainWindow = App (title = "Automation System for Mushroom Production", width=800, height=480, bg = "#ffffff")
ASMP = Main_App(mainWindow)
ASMP.update_actuator()
mainWindow.repeat(1000, ASMP.update_value)
mainWindow.display()

#When the GUI is closed we set finish to "True"
finish = True
backGroundProcess.join()