import { initializeApp } from 'firebase-admin/app';
import { getDatabase } from 'firebase-admin/database';
import { getMessaging } from 'firebase-admin/messaging';
import { onValueCreated } from 'firebase-functions/v2/database';
import { setGlobalOptions } from 'firebase-functions/v2'

initializeApp();
setGlobalOptions({ region: 'europe-west1' });

const db = getDatabase();
const messaging = getMessaging();

export const sendMessageNotification = onValueCreated(
    '/messages/{conversationId}/{messageId}',
    async (event) => {
        try {
            const message = event.data.val();
            const senderId = message.senderId;
            const text = message.text;
            const conversationId = event.params.conversationId;

            const users = conversationId.split('_');
            const receiverId = users[0] === senderId ? users[1] : users[0];

            const tokenSnapshot = await db.ref(`/users/${receiverId}/fcmToken`).once('value');
            const token = tokenSnapshot.val();
            const senderFullNameSnapshot = await db.ref(`/users/${senderId}/fullname`).once('value');
            const fullname = senderFullNameSnapshot.val();

            if (token && fullname) {
                 const payload = {
                    token: token,
                    data: {
                       senderId: senderId,
                       message: text,
                       senderName: fullname,
                     },
                    notification: {
                        title: `New message form ${fullname}`,
                        body: text,
                    },
                };

                const response = await messaging.send(payload);
                console.log('Successfully sent message:', response);

                if (response.failureCount > 0) {
                    console.error('Failed to send messages to some devices:', response.results);
                    // You might want to implement retry logic or log the failed tokens.
                    response.results.forEach((result, index) => {
                        if (result.error) {
                            console.error(`Failure at index ${index}:`, result.error);
                        }
                    });
                }

                return null;
            } else {
                console.log(`FCM token not found for user ${receiverId} or fullname not found for ${senderId}`);
                return null;
            }
        } catch (error) {
            console.error('Error sending message:', error);
            return null;
        }
    }
);