import pyrebase
from pusher_push_notifications import PushNotifications

firebaseConfig = {
    'apiKey' : "AIzaSyCXG0P94Y1tfHiRfPyLHxebMTA3l30Cfzo",
    'authDomain' : "sep2-881ed.firebaseapp.com",
    'databaseURL' : "https://sep2-881ed-default-rtdb.asia-southeast1.firebasedatabase.app",
    'projectId' : "sep2-881ed",
    'storageBucket' : "sep2-881ed.appspot.com",
    'messagingSenderId' : "973612473858",
    'appId' : "1:973612473858:web:ed409b3a5a2efae279911b"
  }

beams_client = PushNotifications(
    instance_id='82e5ff89-52a0-4850-98de-584f6e4e2774',
    secret_key='EF18C9F528A7BE3E334AD0328AF921268CE3C6820B14D701397C2E095D25C4E2',
)


firebase = pyrebase.initialize_app(firebaseConfig)
db = firebase.database()

def water_stream_handler(message):
    if(message['data'] == 0):
        response = beams_client.publish_to_interests(
            interests=['hello'],
            publish_body={
                'apns': {
                'aps': {
                    'alert': {
                    'title': 'Hello',
                    'body': 'Held!',
                    },
                },
                },
                'fcm': {
                'notification': {
                    'title': 'Water Level',
                    'body': 'You need to top up on water in water tank',
                },
                },
                'web': {
                'notification': {
                    'title': 'Hello',
                    'body': 'Hd!',
                },
                },
            },
        )
        
def nutrient_stream_handler(message):
    if(message['data'] == 0):
        response = beams_client.publish_to_interests(
            interests=['hello'],
            publish_body={
                'apns': {
                'aps': {
                    'alert': {
                    'title': 'Hello',
                    'body': 'Held!',
                    },
                },
                },
                'fcm': {
                'notification': {
                    'title': 'Nutrient Level',
                    'body': 'You need to top up on nutrient solution',
                },
                },
                'web': {
                'notification': {
                    'title': 'Hello',
                    'body': 'Hd!',
                },
                },
            },
        )

water_stream = db.child("WaterLevel").stream(water_stream_handler, None)
nutrient_stream = db.child("NutrientLevel").stream(nutrient_stream_handler, None)