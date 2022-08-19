package com.example.tenutotutor;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;
import com.leff.midi.util.MetronomeTick;
import com.leff.midi.util.MidiEventListener;
import com.leff.midi.util.MidiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CustomMidiProcessor
{
    private static final int PROCESS_RATE_MS = 8;

    private HashMap<Class<? extends MidiEvent>, List<MidiEventListener>> mEventsToListeners;
    private HashMap<MidiEventListener, List<Class<? extends MidiEvent>>> mListenersToEvents;

    private MidiFile mMidiFile;
    public boolean mRunning;
    private double mTicksElapsed;
    private long mMsElapsed;
    public long barLength;
    public boolean finishedPlay = false;

    public int mMPQN;
    public int mPPQ;

    private MetronomeTick mMetronome;
    private MidiTrackEventQueue[] mEventQueues;

    public CustomMidiProcessor(MidiFile input)
    {
        this(input, Tempo.DEFAULT_BPM);
    }

    public CustomMidiProcessor(MidiFile input, float BPM)
    {
        mMidiFile = input;

        mMPQN = (int) (60000000 / BPM);
        mPPQ = mMidiFile.getResolution();

        barLength = mPPQ * 4; // TODO: Fix me


        mEventsToListeners = new HashMap<Class<? extends MidiEvent>, List<MidiEventListener>>();
        mListenersToEvents = new HashMap<MidiEventListener, List<Class<? extends MidiEvent>>>();

        mMetronome = new MetronomeTick(new TimeSignature(), mPPQ);

        this.reset();
    }

    public CustomMidiProcessor(CustomMidiProcessor other, float BPM)
    {
        mEventsToListeners = other.mEventsToListeners;
        mListenersToEvents = other.mListenersToEvents;
        mMidiFile = other.mMidiFile;
        mRunning = other.mRunning;
        mTicksElapsed = other.mTicksElapsed;
        mMsElapsed = other.mMsElapsed;
        barLength = other.barLength;
        mPPQ = other.mPPQ;
        mMetronome = other.mMetronome;
        mEventQueues = other.mEventQueues;

        mMPQN = (int) (60000000 / BPM);
    }

    public synchronized void start()
    {
        if(mRunning)
            return;
        mRunning = true;
        new Thread(new Runnable()
        {
            public void run()
            {
                process();
            }
        }).start();
    }

    public synchronized void stop()
    {
        mRunning = false;
    }
    

    public void reset()
    {

        mRunning = false;
        mTicksElapsed = 0;
        mMsElapsed = 0;
        finishedPlay = false;

        mMetronome.setTimeSignature(new TimeSignature());

        List<MidiTrack> tracks = mMidiFile.getTracks();

        if(mEventQueues == null)
        {
            mEventQueues = new MidiTrackEventQueue[tracks.size()];
        }

        for(int i = 0; i < tracks.size(); i++)
        {
            mEventQueues[i] = new MidiTrackEventQueue(tracks.get(i));
        }
    }

    public void startFromTick(long tick)
    {

        mRunning = false;
        mTicksElapsed = tick;
        mMsElapsed = MidiUtil.ticksToMs(tick, mMPQN, mPPQ);

        mMetronome.setTimeSignature(new TimeSignature());

        List<MidiTrack> tracks = mMidiFile.getTracks();

        if(mEventQueues == null)
        {
            mEventQueues = new MidiTrackEventQueue[tracks.size()];
        }

        for(int i = 0; i < tracks.size(); i++)
        {
            mEventQueues[i] = new MidiTrackEventQueue(tracks.get(i));
            mEventQueues[i].getNextEventsUpToTick(tick);
        }
        start();
    }

    public long getLengthInBars(){
        return (mMidiFile.getLengthInTicks() + (barLength - 1)) / barLength;
    }

    public synchronized boolean loop(long startTick, long endTick)
    {
        boolean hasEvents = false;
        mRunning = false;
        finishedPlay = false;
        mTicksElapsed = startTick;
        mMsElapsed = MidiUtil.ticksToMs(startTick, mMPQN, mPPQ);

        mMetronome.setTimeSignature(new TimeSignature());

        List<MidiTrack> tracks = mMidiFile.getTracks();

        if(mEventQueues == null)
        {
            mEventQueues = new MidiTrackEventQueue[tracks.size()];
        }

        for(int i = 0; i < tracks.size(); i++)
        {
            mEventQueues[i] = new MidiTrackEventQueue(tracks.get(i), endTick);
            mEventQueues[i].getNextEventsUpToTick(startTick);
        }

        return hasEvents;
    }

    public boolean isStarted()
    {
        return mTicksElapsed > 0;
    }

    public boolean isRunning()
    {
        return mRunning;
    }

    protected void onStart(boolean fromBeginning)
    {

        Iterator<MidiEventListener> it = mListenersToEvents.keySet().iterator();

        while(it.hasNext())
        {

            MidiEventListener mel = it.next();
            mel.onStart(fromBeginning);
        }
    }

    protected void onStop(boolean finished)
    {

        Iterator<MidiEventListener> it = mListenersToEvents.keySet().iterator();

        while(it.hasNext())
        {

            MidiEventListener mel = it.next();
            mel.onStop(finished);
        }
    }

    public void registerEventListener(MidiEventListener mel, Class<? extends MidiEvent> event)
    {

        List<MidiEventListener> listeners = mEventsToListeners.get(event);
        if(listeners == null)
        {

            listeners = new ArrayList<MidiEventListener>();
            listeners.add(mel);
            mEventsToListeners.put(event, listeners);
        }
        else
        {
            listeners.add(mel);
        }

        List<Class<? extends MidiEvent>> events = mListenersToEvents.get(mel);
        if(events == null)
        {

            events = new ArrayList<Class<? extends MidiEvent>>();
            events.add(event);
            mListenersToEvents.put(mel, events);
        }
        else
        {
            events.add(event);
        }
    }

    public void unregisterEventListener(MidiEventListener mel)
    {

        List<Class<? extends MidiEvent>> events = mListenersToEvents.get(mel);
        if(events == null)
        {
            return;
        }

        for(Class<? extends MidiEvent> event : events)
        {

            List<MidiEventListener> listeners = mEventsToListeners.get(event);
            listeners.remove(mel);
        }

        mListenersToEvents.remove(mel);
    }

    public void unregisterEventListener(MidiEventListener mel, Class<? extends MidiEvent> event)
    {

        List<MidiEventListener> listeners = mEventsToListeners.get(event);
        if(listeners != null)
        {
            listeners.remove(mel);
        }

        List<Class<? extends MidiEvent>> events = mListenersToEvents.get(mel);
        if(events != null)
        {
            events.remove(event);
        }
    }

    public void unregisterAllEventListeners()
    {
        mEventsToListeners.clear();
        mListenersToEvents.clear();
    }

    protected void dispatch(MidiEvent event)
    {

        // Tempo and Time Signature events are always needed by the processor
        if(event.getClass().equals(Tempo.class))
        {
            mMPQN = ((Tempo) event).getMpqn();
        }
        else if(event.getClass().equals(TimeSignature.class))
        {

            boolean shouldDispatch = mMetronome.getBeatNumber() != 1;
            mMetronome.setTimeSignature((TimeSignature) event);

            if(shouldDispatch)
            {
                dispatch(mMetronome);
            }
        }

        this.sendOnEventForClass(event, event.getClass());
        this.sendOnEventForClass(event, MidiEvent.class);
    }

    private void sendOnEventForClass(MidiEvent event, Class<? extends MidiEvent> eventClass)
    {

        List<MidiEventListener> listeners = mEventsToListeners.get(eventClass);

        if(listeners == null)
        {
            return;
        }

        for(MidiEventListener mel : listeners)
        {
            mel.onEvent(event, mMsElapsed);
        }
    }

    private void process()
    {

        onStart(mTicksElapsed < 1);

        long lastMs = System.currentTimeMillis();

        boolean finished = false;

        while(mRunning)
        {

            long now = System.currentTimeMillis();
            long msElapsed = now - lastMs;

            if(msElapsed < PROCESS_RATE_MS)
            {
                try
                {
                    Thread.sleep(PROCESS_RATE_MS - msElapsed);
                }
                catch(Exception e)
                {
                }
                continue;
            }

            double ticksElapsed = MidiUtil.msToTicks(msElapsed, mMPQN, mPPQ);

            if(ticksElapsed < 1)
            {
                continue;
            }

            if(mMetronome.update(ticksElapsed))
            {
                dispatch(mMetronome);
            }

            lastMs = now;
            mMsElapsed += msElapsed;
            mTicksElapsed += ticksElapsed;

            boolean more = false;
            for(int i = 0; i < mEventQueues.length; i++)
            {

                MidiTrackEventQueue queue = mEventQueues[i];
                if(!queue.hasMoreEvents())
                {
                    continue;
                }

                ArrayList<MidiEvent> events = queue.getNextEventsUpToTick(mTicksElapsed);
                for(MidiEvent event : events)
                {
                    this.dispatch(event);
                }

                if(queue.hasMoreEvents())
                {
                    more = true;
                }
            }
            //System.out.println("Has more: " + more);
            if(!more)
            {
                finished = true;
                break;
            }

        }
        finishedPlay = finished;
        mRunning = false;
        onStop(finished);
    }

    private class MidiTrackEventQueue
    {

        private MidiTrack mTrack;
        private Iterator<MidiEvent> mIterator;
        private ArrayList<MidiEvent> mEventsToDispatch;
        private MidiEvent mNext;
        private long endtick;

        public MidiTrackEventQueue(MidiTrack track)
        {
            this(track, Long.MAX_VALUE);
        }

        public MidiTrackEventQueue(MidiTrack track, long endtick)
        {
            this.endtick = endtick;
            mTrack = track;

            mIterator = mTrack.getEvents().iterator();
            mEventsToDispatch = new ArrayList<MidiEvent>();

            if(mIterator.hasNext())
            {
                mNext = mIterator.next();
            }
        }

        public ArrayList<MidiEvent> getNextEventsUpToTick(double tick)
        {

            mEventsToDispatch.clear();

            while(mNext != null)
            {

                if(mNext.getTick() <= tick)
                {
                    if (mNext.getTick() > endtick){
                        mNext = null;
                        break;
                    }

                    mEventsToDispatch.add(mNext);

                    if(mIterator.hasNext())
                    {
                        mNext = mIterator.next();
                    }
                    else
                    {
                        mNext = null;
                    }
                }
                else
                {
                    break;
                }
            }

            return mEventsToDispatch;
        }

        public boolean hasMoreEvents()
        {
            return mNext != null;
        }
    }
}
