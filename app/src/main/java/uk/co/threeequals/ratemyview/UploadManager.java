package uk.co.threeequals.ratemyview;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.alexbbb.uploadservice.UploadRequest;
import com.alexbbb.uploadservice.UploadService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by michaelwaterworth on 21/07/15.
 */
public class UploadManager extends BroadcastReceiver {
    final String TAGLISTEN = "Broadcast Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {
            if (UploadService.getActionBroadcast().equals(intent.getAction())) {
                final int status = intent.getIntExtra(UploadService.STATUS, 0);
                final String uploadId = intent.getStringExtra(UploadService.UPLOAD_ID);

                switch (status) {
                    case UploadService.STATUS_COMPLETED:
                        final int responseCode = intent.getIntExtra(UploadService.SERVER_RESPONSE_CODE, 0);
                        final String responseMsg = intent.getStringExtra(UploadService.SERVER_RESPONSE_MESSAGE);
                        onCompleted(context, uploadId, responseCode, responseMsg);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    public static void processQueue(Context context){
        List<RmVOverlayItem> views = RmVOverlayItem.listAll(RmVOverlayItem.class);
        for(RmVOverlayItem r : views){
            upload(context, r);
        }
    }

    public static int getQueueLength(){
        List<RmVOverlayItem> views = RmVOverlayItem.listAll(RmVOverlayItem.class);
        return views.size();
    }

    public void onCompleted(Context context,
                            String uploadId,
                            int serverResponseCode,
                            String serverResponseMessage) {
        Log.i(TAGLISTEN, "Upload with ID " + uploadId
                + " has been completed with HTTP " + serverResponseCode
                + ". Response from server: " + serverResponseMessage);

        //if(serverResponseCode == 200) {

            //Listen to all events
            //On success of upload grab Id and remove from Db
            //TODO - Check JSON response
            RmVOverlayItem uploaded = RmVOverlayItem.findById(RmVOverlayItem.class, Long.parseLong(uploadId));
            if(uploaded != null) {
                uploaded.delete();
            }

        try {
            JSONObject jsonObject = new JSONObject(serverResponseMessage);
            RmVOverlayItem rmVOverlayItem = MapsFragment.parseOverlayItem(jsonObject);
            buildSuccessNotification(context, rmVOverlayItem);

        } catch (JSONException e) {
            e.printStackTrace();
        }


            //If your server responds with a JSON, you can parse it
            //from serverResponseMessage string using a library
            //such as org.json (embedded in Android) or google's gson
        //}
    }

    static public void buildSuccessNotification(Context context, RmVOverlayItem rmvOverlayItem){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.app_icon_silhouette)
                        .setContentTitle("Upload Successfull")
                        .setContentText("Click to see uploaded view");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, TheirViewActivity.class);
        resultIntent.putExtra("object", rmvOverlayItem);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(TheirViewActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //TODO - Fix issue of replacing previous notification
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }

    static public void upload(Context context, RmVOverlayItem rmvOverlayItem) {
        final UploadRequest request = new UploadRequest(context,
                rmvOverlayItem.getId() + "",//Long used to keep track of db
                context.getString(R.string.base_url) + context.getString(R.string.upload_path));

    /*
     * parameter-name: is the name of the parameter that will contain file's data.
     * Pass "uploaded_file" if you're using the test PHP script
     *
     * custom-file-name.extension: is the file name seen by the server.
     * E.g. value of $_FILES["uploaded_file"]["name"] of the test PHP script
     */

        request.addFileToUpload(rmvOverlayItem.getPhotoLocation(),
                "image",
                "uploaded.jpg",
                "image/jpeg");

        //and parameters
        request.addParameter("comments", rmvOverlayItem.getComments());
        request.addArrayParameter("words", rmvOverlayItem.getWordsArray());
        request.addParameter("age", rmvOverlayItem.getAge());
        request.addParameter("know", rmvOverlayItem.getKnow());
        request.addParameter("rating", "" + rmvOverlayItem.getRating());
        request.addParameter("heading", "" + rmvOverlayItem.getHeading());

        //Location
        request.addParameter("lat", "" + rmvOverlayItem.getLat());
        request.addParameter("lng", "" + rmvOverlayItem.getLng());

        //configure the notification
        request.setNotificationConfig(R.drawable.app_icon_silhouette,
                context.getString(R.string.app_name),
                context.getString(R.string.uploading_toast),
                context.getString(R.string.uploading_success),
                context.getString(R.string.upload_failed),
                true);//Clear on success

        // set the intent to perform when the user taps on the upload notification.
        // currently tested only with intents that launches an activity
        // if you comment this line, no action will be performed when the user taps on the notification
        request.setNotificationClickIntent(new Intent(context.getApplicationContext(), BaseActivity.class).putExtra("upload", "intent"));

        try {
            //Start upload service and display the notification
            UploadService.startUpload(request);

        } catch (Exception exc) {
            //You will end up here only if you pass an incomplete UploadRequest
            Log.e("AndroidUploadService", exc.getLocalizedMessage(), exc);
        }
        Toast.makeText(context.getApplicationContext(), context.getString(R.string.uploading_toast), Toast.LENGTH_LONG).show();
    }
}
