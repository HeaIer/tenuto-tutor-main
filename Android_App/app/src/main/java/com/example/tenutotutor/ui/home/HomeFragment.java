package com.example.tenutotutor.ui.home;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.tenutotutor.MainActivity;
import com.example.tenutotutor.R;
import com.example.tenutotutor.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class HomeFragment extends Fragment {
    private static final UUID[] MIDI_UUIDS = new UUID[]{
            UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")
    };
    public static String DEVICE_NAME = "Tenuto";
    private static final String TAG = "MidiBtlePairing";
    private FragmentHomeBinding binding;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private static boolean mScanning;
    private Handler mHandler;
    private BluetoothDevice bluetoothDevice;

    private MidiManager mMidiManager;
    private OpenDeviceListAdapter mOpenDeviceListAdapter;

    static class BluetoothMidiDeviceTracker {
        final public BluetoothDevice bluetoothDevice;
        final public MidiDevice midiDevice;
        public int inputOpenCount;
        public int outputOpenCount;

        /**
         * @param bluetoothDevice
         * @param midiDevice
         */
        public BluetoothMidiDeviceTracker(BluetoothDevice bluetoothDevice,
                                          MidiDevice midiDevice) {
            this.bluetoothDevice = bluetoothDevice;
            this.midiDevice = midiDevice;
        }
    }

    private class OpenDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothMidiDeviceTracker> mOpenDevices;
        private HashMap<MidiDeviceInfo, BluetoothMidiDeviceTracker> mInfoTrackerMap = new HashMap<MidiDeviceInfo, BluetoothMidiDeviceTracker>();

        public OpenDeviceListAdapter() {
            super();
            mOpenDevices = new ArrayList<BluetoothMidiDeviceTracker>();
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void addDevice(BluetoothMidiDeviceTracker deviceTracker) {
            if (!mOpenDevices.contains(deviceTracker)) {
                mOpenDevices.add(deviceTracker);
                MidiDeviceInfo info = deviceTracker.midiDevice.getInfo();
                mInfoTrackerMap.put(info, deviceTracker);
                notifyDataSetChanged();
            }
        }

        /**
         * @param info
         */
        public void remove(MidiDeviceInfo info) {
            BluetoothMidiDeviceTracker deviceTracker = mInfoTrackerMap
                    .get(info);
            if (deviceTracker != null) {
                mOpenDevices.remove(deviceTracker);
                notifyDataSetChanged();
            }
        }

        public BluetoothMidiDeviceTracker getDevice(int position) {
            return mOpenDevices.get(position);
        }

        public BluetoothMidiDeviceTracker getDevice(MidiDeviceInfo info) {
            return mInfoTrackerMap.get(info);
        }

        public void clear() {
            mOpenDevices.clear();
            mInfoTrackerMap.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mOpenDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mOpenDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            return null;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mHandler = new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            setupMidi();
        } else {
            Toast.makeText(getContext(), "MIDI not supported!",
                    Toast.LENGTH_LONG).show();
        }
        mOpenDeviceListAdapter = new OpenDeviceListAdapter();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3000);
                this.startActivity(intent);

                mBluetoothAdapter.enable();
            }
        }
        scanLeDevice(true);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ImageView info = binding.info;
        info.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), InfoActivity.class);
            startActivity(intent);
        });
        TextView connectValue = binding.homeConnectstatus;
        Button connect = binding.homeConnectbutton;
        connect.setOnClickListener(view -> {
            if (bluetoothDevice != null) {
                Log.i(TAG, "Bluetooth device name = "
                        + bluetoothDevice.getName()
                        + ", address = "
                        + bluetoothDevice.getAddress());
                mMidiManager.openBluetoothDevice(bluetoothDevice,
                        new MidiManager.OnDeviceOpenedListener() {
                            @Override
                            public void onDeviceOpened(MidiDevice device) {
                                onBluetoothDeviceOpen(bluetoothDevice, device);
                                MainActivity.setMidiInputPort(device.openInputPort(0));
                            }
                        }, null);

            }
            updateConnectLabel(true);
        });
        updateConnectLabel(false);

        return root;
    }

    private void updateConnectLabel(boolean showFailed) {
        TextView connectValue = binding.homeConnectstatus;
        if (MainActivity.getSuccess()) {
            connectValue.setTextColor(Color.GREEN);
            connectValue.setText("Connected");
        } else if (showFailed) {
            connectValue.setText("Failed");
            connectValue.setTextColor(Color.RED);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupMidi() {
        // Setup MIDI
        mMidiManager = (MidiManager) getActivity().getSystemService(Context.MIDI_SERVICE);

        mMidiManager.registerDeviceCallback(new MidiManager.DeviceCallback() {
            @Override
            public void onDeviceRemoved(final MidiDeviceInfo info) {
                mOpenDeviceListAdapter.remove(info);
            }

            // Update port open counts so user knows if the device is in use.
            @Override
            public void onDeviceStatusChanged(final MidiDeviceStatus status) {
                MidiDeviceInfo info = status.getDeviceInfo();
                BluetoothMidiDeviceTracker tracker = mOpenDeviceListAdapter
                        .getDevice(info);
                if (tracker != null) {
                    tracker.outputOpenCount = 0;
                    for (int i = 0; i < info.getOutputPortCount(); i++) {
                        tracker.outputOpenCount += status
                                .getOutputPortOpenCount(i);
                    }
                    tracker.inputOpenCount = 0;
                    for (int i = 0; i < info.getInputPortCount(); i++) {
                        tracker.inputOpenCount += status.isInputPortOpen(i) ? 1
                                : 0;
                    }
                    mOpenDeviceListAdapter.notifyDataSetChanged();
                }
            }
        }, new android.os.Handler(Looper.getMainLooper()));
    }

    private void onBluetoothDeviceOpen(final BluetoothDevice bluetoothDevice,
                                       final MidiDevice midiDevice) {
        getActivity().runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                if (midiDevice != null) {
                    BluetoothMidiDeviceTracker tracker = new BluetoothMidiDeviceTracker(
                            bluetoothDevice, midiDevice);
                    mOpenDeviceListAdapter.addDevice(tracker);
                } else {
                    Toast.makeText(getActivity(),
                            "MIDI device open failed!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    String deviceName = device.getName();
                    if (DEVICE_NAME.equals(deviceName)) {
                        bluetoothDevice = device;
                        MainActivity.success = true;
                        if (mScanning) {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            mScanning = false;
                        }
                    }
                }
            };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            startScanningIfPermitted();
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScanningIfPermitted() {
        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            getActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    9500);
        } else {
            startScanningLeDevices();
        }
    }

    private void startScanningLeDevices() {
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                //invalidateOptionsMenu();
            }
        }, 10000);

        mScanning = true;
        mBluetoothAdapter.startLeScan(MIDI_UUIDS, mLeScanCallback);
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private final ArrayList<BluetoothDevice> mLeDevices;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public BluetoothDevice getDeviceByName(String a) {
            for (BluetoothDevice d : mLeDevices) {
                if (d.getName().equals(a)) {
                    return d;
                }
            }
            return null;
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


