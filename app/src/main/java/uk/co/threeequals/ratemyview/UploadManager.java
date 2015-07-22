package uk.co.threeequals.ratemyview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.alexbbb.uploadservice.UploadRequest;
import com.alexbbb.uploadservice.UploadService;

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
                        onCompleted(uploadId, responseCode, responseMsg);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    public static int getQueueLength(){
        List<RmVOverlayItem> views = RmVOverlayItem.listAll(RmVOverlayItem.class);
        return views.size();
    }

    public void onCompleted(String uploadId,
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
            //If your server responds with a JSON, you can parse it
            //from serverResponseMessage string using a library
            //such as org.json (embedded in Android) or google's gson
        //}
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
        request.setNotificationConfig(R.drawable.uploading_icon,
                context.getString(R.string.app_name),
                context.getString(R.string.uploading_toast),
                context.getString(R.string.uploading_success),
                context.getString(R.string.upload_failed),
                false);

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
