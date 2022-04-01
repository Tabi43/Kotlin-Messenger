package com.example.kotlinmessenger;

//https://guides.codepath.com/android/Creating-Custom-Listeners#1-defining-the-interface

import android.util.Log;

public class Semaphore {

    public interface SemaphoreListener{
        public void onReady();
        public void onChange(Integer value);
    }

    private SemaphoreListener listener;
    private Integer value;

    public Semaphore(Integer startValue){
        this.value = startValue;
    }

    public Semaphore(){
        this.value = 0;
    }

    public void addSemaphoreListener(SemaphoreListener newListener){
        this.listener = newListener;
    }

    public void signal(){
        this.value--;
        if(this.value == 0) this.listener.onReady();
        else this.listener.onChange(this.value);
    }

    public void setValue(Integer value){
        Log.d("GEN. SEMAPHORE","Semaforo settato su "+value);
        this.value = value;
    }

}