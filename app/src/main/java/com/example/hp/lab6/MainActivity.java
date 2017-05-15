package com.example.hp.lab6;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;


public class MainActivity extends Activity implements OnClickListener {

    private Button play_pauseBtn,stopBtn,quitBtn;
    private MusicService musicService;
    private ImageView imageview;
    private TextView state, playing_time,max_time;
    private SeekBar seekbar;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    //Bitmap bmp = BitmapFactory.decodeResource(this.getResources(),R.mipmap.image);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//绑定布局
        findView();//findIdByView，找到个个控件
        bindButton();//按钮绑定监听器。
        musicService=new MusicService();//新建一个MusicService后台服务
        connection();//绑定后台服务
        max_time.setText(time.format(musicService.mediaPlayer.getDuration()));//设置max_time这个textview的内容是歌曲的时间长度
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.tip);
        LinearInterpolator lin = new LinearInterpolator();//LinearInterpolator为匀速效果
        operatingAnim.setInterpolator(lin);//setInterpolator表示设置旋转速率。
        imageview.startAnimation(operatingAnim);//开始旋转

    }

    private void findView(){
        /** 通过id获得各种组件 */
        play_pauseBtn=(Button) findViewById(R.id.play_pause);//开始/暂停按钮
        stopBtn=(Button) findViewById(R.id.stop);//停止按钮
        quitBtn=(Button) findViewById(R.id.quit);//退出按钮
        state=(TextView) findViewById(R.id.state);//seekbar上面表示状态的文字textview
        playing_time=(TextView) findViewById(R.id.playing_time);//seekbar左边现在播放的时间
        max_time=(TextView) findViewById(R.id.song_time);//播放的这首歌曲的时长
        seekbar=(SeekBar) findViewById(R.id.seekbar);//seekbar
        imageview=(ImageView ) findViewById(R.id.imageview);//旋转的图片
    }

    private void bindButton(){
        /** 为按钮添加监听 ，三个按钮的监听器都是MainActivity*/
        play_pauseBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        quitBtn.setOnClickListener(this);
    }
    private ServiceConnection sc=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService=((MusicService.MyBinder)service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService=null;
        }
    };

    Handler mHandler=new Handler(); //创建Handler对象
    //要用handler来处理多线程可以使用runnable接口，这里先定义该接口，线程中运行该接口的run函数
    Runnable mRunnable=new Runnable() { //新建一个Runnable接口
        @Override
        public void run() {//将要执行的操作写在线程对象的run方法当中

            //Matrix mtx = new Matrix();
            //mtx.postRotate(1);
            // Rotating Bitmap
            //Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mtx, true);
            //BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);
            //imageview.setImageDrawable(bmd);

            //设置seekbar左边的playing_time textview为当前歌曲播放到的时间
            playing_time.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
            //设置seekbar为当前歌曲播放到的时间
            seekbar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            //seekbar条拖动监听事件
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override//seekbar发生改变时
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser){
                        musicService.mediaPlayer.seekTo(seekBar.getProgress());
                    }//根据seekbar拖动到的位置改变设置歌曲播放到的进度
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            mHandler.postDelayed(mRunnable,100);//100ms执行一次tun（）函数
        }
    };
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.play_pause://点击play_pause按钮事件
                musicService.playORpuase();//调用musicService中的playORpause函数，暂停或开始音乐播放
                if(musicService.mediaPlayer.isPlaying()){
                    state.setText("Playing");//当音乐在播放时，我们设置state的内容为Playing
                    play_pauseBtn.setText("PAUSE");//设置button的内容为Puase
                    //Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.tip);
                    //LinearInterpolator lin = new LinearInterpolator();//LinearInterpolator为匀速效果
                    //operatingAnim.setInterpolator(lin);//setInterpolator表示设置旋转速率。
                   // imageview.startAnimation(operatingAnim);//开始旋转
                }
                else{
                    state.setText("Paused");//当音乐停止时，设置state内容为Paused
                    play_pauseBtn.setText("PLAY");//button内容为Play
                }
                break;
            case R.id.stop://点击暂停
                musicService.stop();//关闭音乐
                play_pauseBtn.setText("PLAY");//设置开始和暂停按钮的内容为Play
                state.setText("Stop");//设置state为Stop
                //imageview.clearAnimation();
                break;
            case R.id.quit://点击退出
                mHandler.removeCallbacks(mRunnable);//回调mRunnable接口
                unbindService(sc);//解除后台服务的绑定
                try{
                    MainActivity.this.finish();//结束当前的时间
                    System.exit(0);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }

    }

    private void connection(){
        //启动指定的Service
        Intent intent=new Intent(this ,MusicService.class);
        bindService(intent,sc,BIND_AUTO_CREATE);//绑定服务
    }

    @Override
    protected void onResume() {
        seekbar.setProgress(musicService.mediaPlayer.getCurrentPosition());
        seekbar.setMax(musicService.mediaPlayer.getDuration());
        mHandler.post(mRunnable);//通过Handler启动Runnable
        super.onResume();
    }

}
