package com.example.starter;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

// Class to access speakers
public class AudioSpeaker extends Thread {

    public interface SampleGenerator{
        short[] generate();
    }

    AudioTrack track;
    int SamplingFreq;
    Context mycontext;
    SampleGenerator sampleGen;
    AudioManager man;
    boolean _pause;

    // set the volume to some ratio of the max
    public void setVolume(float volume)
    {
        man.setStreamVolume(AudioManager.STREAM_MUSIC,(int)(volume*man.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),1);
    }

    public AudioSpeaker(Context mycontext,int samplingFreq, int sampleLen, SampleGenerator sampleGen)
    {
        this.mycontext = mycontext;
        man = (AudioManager)mycontext.getSystemService(Context.AUDIO_SERVICE);
        SamplingFreq = samplingFreq;
        this.sampleGen=sampleGen;
        track = new AudioTrack(AudioManager.STREAM_MUSIC,SamplingFreq,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,sampleLen*3,AudioTrack.MODE_STREAM);
        track.play();
        _pause=false;
    }

    // skip t time for the signal generator
    public void skip(int t) throws  InterruptedException{
        try {
            Thread.sleep(t * 1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void run()
    {
        setVolume(0.7f);
        while(!_pause) {
            short[] s = sampleGen.generate();
            track.write(s, 0, s.length);
        }
    }

    public void pause()
    {
        this._pause=true;
        try {
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
