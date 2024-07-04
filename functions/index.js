const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendAppointmentNotification = functions.database.ref("/appointments/{appointmentId}")
  .onCreate((snapshot, context) => {
    const appointment = snapshot.val();

    const payload = {
      notification: {
        title: "New Appointment Booked",
        body: `Appointment for ${appointment.fullName} on ${appointment.dateOfAppointment}`,
        sound: "default",
      },
    };

    return admin.messaging().sendToTopic("appointments", payload);
  });
