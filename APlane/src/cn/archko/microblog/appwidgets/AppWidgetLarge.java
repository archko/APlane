package cn.archko.microblog.appwidgets;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import cn.archko.microblog.R;
import cn.archko.microblog.service.AKWidgetService;
import cn.archko.microblog.ui.NewStatusActivity;
import cn.archko.microblog.ui.SplashActivity;
import com.me.microblog.bean.Status;
import com.me.microblog.util.DateUtils;
import com.me.microblog.util.WeiboLog;

/**
 * 4x2 App-Widget
 *
 * @author archko
 */
@SuppressLint("NewApi")
public class AppWidgetLarge extends AppWidgetProvider {

    public static final String TAG="AppWidgetLarge";

    public static final String CMDAPPWIDGETUPDATE="app_widget_large_update";

    private static AppWidgetLarge mInstance;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Intent intent=new Intent(context, AKWidgetService.class);
        context.startService(intent);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent intent=new Intent(context, AKWidgetService.class);
        context.stopService(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        WeiboLog.d(TAG, "onReceive."+intent);
        /*intent=new Intent(context, AKWidgetService.class);
        context.startService(intent);*/
        final Intent updateIntent=new Intent(AKWidgetService.SERVICECMD);
        updateIntent.setAction(intent.getAction());
        updateIntent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
        //defaultAppWidget(context, null);
    }

    public static synchronized AppWidgetLarge getInstance() {
        if (mInstance==null) {
            mInstance=new AppWidgetLarge();
        }
        return mInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        defaultAppWidget(context, appWidgetIds);
        final Intent updateIntent=new Intent(AKWidgetService.SERVICECMD);
        updateIntent.putExtra(AKWidgetService.CMDNAME, AppWidgetLarge.CMDAPPWIDGETUPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
    }

    /**
     * Initialize given widgets to default state, where we launch Music on
     * default click and hide actions if service not running.
     */
    private void defaultAppWidget(final Context context, final int[] appWidgetIds) {
        final RemoteViews appWidgetViews=new RemoteViews(context.getPackageName(),
            R.layout.ak_app_widget_large);
        linkButtons(context, appWidgetViews);
        pushUpdate(context, appWidgetIds, appWidgetViews);
    }

    private void pushUpdate(final Context context, final int[] appWidgetIds, final RemoteViews views) {
        final AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
        if (appWidgetIds!=null) {
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        } else {
            appWidgetManager.updateAppWidget(new ComponentName(context, getClass()), views);
        }
    }

    /**
     * Check against {@link android.appwidget.AppWidgetManager} if there are any instances of this
     * widget.
     */
    private boolean hasInstances(final Context context) {
        final AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
        final int[] mAppWidgetIds=appWidgetManager.getAppWidgetIds(new ComponentName(context,
            getClass()));
        return mAppWidgetIds.length>0;
    }

    /**
     * Handle a change notification coming over from
     * {@link MusicPlaybackService}
     */
    /*public void notifyChange(final MusicPlaybackService service, final String what) {
        if (hasInstances(service)) {
            if (MusicPlaybackService.META_CHANGED.equals(what)
                    || MusicPlaybackService.PLAYSTATE_CHANGED.equals(what)) {
                performUpdate(service, null);
            }
        }
    }*/

    /**
     * Update all active widget instances by pushing changes
     */
    /*public void performUpdate(final MusicPlaybackService service, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(service.getPackageName(),
                R.layout.app_widget_large);

        final CharSequence trackName = service.getTrackName();
        final CharSequence artistName = service.getArtistName();
        final CharSequence albumName = service.getAlbumName();
        final Bitmap bitmap = service.getAlbumArt();

        // Set the titles and artwork
        appWidgetView.setTextViewText(R.id.app_widget_large_line_one, trackName);
        appWidgetView.setTextViewText(R.id.app_widget_large_line_two, artistName);
        appWidgetView.setTextViewText(R.id.app_widget_large_line_three, albumName);
        appWidgetView.setImageViewBitmap(R.id.app_widget_large_image, bitmap);

        // Set correct drawable for pause state
        final boolean isPlaying = service.isPlaying();

        // Link actions buttons to intents
        linkButtons(service, appWidgetView, isPlaying);

        // Update the app-widget
        pushUpdate(service, appWidgetIds, appWidgetView);

        // Build the notification
        if (ApolloUtils.isApplicationSentToBackground(service)) {
            service.mBuildNotification = true;
        }
    }*/

    /**
     * Link up various button actions using {@link PendingIntents}.
     *
     * @param playerActive True if player is active in background, which means
     *                     widget click will launch {@link AudioPlayerActivity},
     *                     otherwise we launch {@link MusicBrowserActivity}.
     */
    private void linkButtons(final Context context, final RemoteViews views) {
        Intent action;
        PendingIntent pendingIntent;

        final ComponentName serviceName=new ComponentName(context, AKWidgetService.class);

        // Previous track
        //action=new Intent(AKWidgetService.PREVIOUS_ACTION);
        //action.setComponent(serviceName);
        action=new Intent(context, AppWidgetLarge.class);
        action.setAction(AKWidgetService.PREVIOUS_ACTION);
        //pendingIntent=PendingIntent.getActivity(context, 0, action, 0);   //调用Activity
        pendingIntent=PendingIntent.getBroadcast(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);    //自身的更新onReceive接收
        views.setOnClickPendingIntent(R.id.app_widget_large_previous, pendingIntent);

        // Play and pause
        //action=new Intent(AKWidgetService.TOGGLEPAUSE_ACTION);
        //action.setComponent(serviceName);
        action=new Intent(context, AppWidgetLarge.class);
        action.setAction(AKWidgetService.TOGGLEPAUSE_ACTION);
        //pendingIntent=PendingIntent.getActivity(context, 0, action, 0);
        pendingIntent=PendingIntent.getBroadcast(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.app_widget_large_play, pendingIntent);

        // Next track
        //action=new Intent(AKWidgetService.NEXT_ACTION);
        //action.setComponent(serviceName);
        action=new Intent(context, AppWidgetLarge.class);
        action.setAction(AKWidgetService.NEXT_ACTION);
        //pendingIntent=PendingIntent.getActivity(context, 0, action, 0);
        pendingIntent=PendingIntent.getBroadcast(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.app_widget_large_next, pendingIntent);

        //
        action=new Intent(context, SplashActivity.class);
        pendingIntent=PendingIntent.getActivity(context, 0, action, 0);
        views.setOnClickPendingIntent(R.id.app_widget_large_image, pendingIntent);

        //
        action=new Intent(context, NewStatusActivity.class);
        pendingIntent=PendingIntent.getActivity(context, 0, action, 0);
        views.setOnClickPendingIntent(R.id.app_widget_large_edit, pendingIntent);
    }

    public void performUpdate(AKWidgetService service, final int[] appWidgetIds) {
        final RemoteViews appWidgetView=new RemoteViews(service.getPackageName(),
            R.layout.ak_app_widget_large);
        updateTxt(service, appWidgetView);

        //appWidgetView.setImageViewBitmap(R.id.app_widget_large_image, bitmap);

        // Link actions buttons to intents
        linkButtons(service, appWidgetView);

        // Update the app-widget
        pushUpdate(service, appWidgetIds, appWidgetView);
    }

    private void updateTxt(AKWidgetService service, RemoteViews appWidgetView) {
        Status status=service.getStatus();
        WeiboLog.v(TAG, "status:"+status);
        if (null==status) {
            return;
        }

        // Set the titles and artwork
        try {
            appWidgetView.setTextViewText(R.id.app_widget_large_line_one, status.user.screenName);
            appWidgetView.setTextViewText(R.id.app_widget_large_line_two, DateUtils.getDateString(status.createdAt));
            appWidgetView.setTextViewText(R.id.app_widget_large_line_three, status.text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
