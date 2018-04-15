package me.yun.music.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;

import java.io.File;

import me.yun.music.R;
import me.yun.music.application.AppCache;
import me.yun.music.constants.RxBusTags;
import me.yun.music.executor.DownloadMusicInfo;
import me.yun.music.utils.ToastUtils;
import me.yun.music.utils.id3.ID3TagUtils;
import me.yun.music.utils.id3.ID3Tags;

/**
 * 下载完成广播接收器
 */
public class DownloadReceiver extends BroadcastReceiver {
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        DownloadMusicInfo downloadMusicInfo = AppCache.get().getDownloadList().get(id);
        if (downloadMusicInfo != null) {
            ToastUtils.show(context.getString(R.string.download_success, downloadMusicInfo.getTitle()));

            String musicPath = downloadMusicInfo.getMusicPath();
            String coverPath = downloadMusicInfo.getCoverPath();
            if (!TextUtils.isEmpty(musicPath) && !TextUtils.isEmpty(coverPath)) {
                // 设置专辑封面
                File musicFile = new File(musicPath);
                File coverFile = new File(coverPath);
                if (musicFile.exists() && coverFile.exists()) {
                    ID3Tags id3Tags = new ID3Tags.Builder()
                            .setCoverFile(coverFile)
                            .build();
                    ID3TagUtils.setID3Tags(musicFile, id3Tags, false);
                }
            }

            // 由于系统扫描音乐是异步执行，因此延迟刷新音乐列表
            mHandler.postDelayed(() -> RxBus.get().post(RxBusTags.SCAN_MUSIC, new Object()), 1000);
        }
    }
}
