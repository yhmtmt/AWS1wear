package com.example.yhmtmt.aws2;

import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import CommandService.CommandGrpc;
import CommandService.CommandOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class MainActivity extends WearableActivity implements View.OnClickListener {
    public static final String PREFS_NAME = "aws2PrefsFile";

    // for communication
    String host = "192.168.128.3";
    int port = 50051;

    private ManagedChannel chan_msg, chan_cmd;
    private CommandGrpc.CommandBlockingStub stub_msg;
    private CommandGrpc.CommandBlockingStub stub_cmd;

    int hbt = 0;

    Button btnStarboard;
    Button btnPort;
    Button btnMidship;
    Button btnForward;
    Button btnBackward;
    Button btnNeutral;

    SeekBar skbRudder;
    SeekBar skbEngine;

    TextView txtInfo1;
    TextView txtInfo2;

    enum EngState { flas, hfas, slas, dsas, stp, dsah, slah, hfah, flah, nf};
    EngState engState, nextEngState;
    enum RudState { hap, p20, p10, mds, s10, s20, has };
    RudState rudState, nextRudState;
    int eng, rud, rev, sog, cog, yaw, dpt;

    //    private TextView mTextView;
    class MessageWatcher extends Thread {
        public void run() {
            CommandOuterClass.FltrMsgReq req = CommandOuterClass.FltrMsgReq.newBuilder()
                    .setInstName("ui_manager")
                    .setPeriod(0.5)
                    .build();
            Iterator<CommandOuterClass.FltrMsg> msgs;
            msgs = stub_msg.watchFltrMsg(req);
            while(msgs.hasNext()){
                CommandOuterClass.FltrMsg msg = msgs.next();
                Filter.UIManagerMsg.UIManagerMsg  fmsg =
                        Filter.UIManagerMsg.UIManagerMsg.getRootAsUIManagerMsg(msg.getMessage().asReadOnlyByteBuffer());
                rev = (int)fmsg.engine().rev();
                eng = (int)fmsg.control().eng();
                rud = (int)fmsg.control().rud();
                sog = (int)(fmsg.velocity().sog() * 10);
                cog = (int)(fmsg.velocity().cog());
                yaw = (int)(fmsg.attitude().yaw());
                if(yaw < 0)
                    yaw += 360;
                if(cog < 0)
                    cog += 360;

                dpt = (int)(fmsg.depth() * 10);
                skbRudder.setProgress(rud);
                skbEngine.setProgress(eng);
                String strInfo1, strInfo2;
                strInfo1 = String.format("C%03d H%03d D%03d.%1dm", cog, yaw, dpt/10, dpt%10);
                strInfo2 = String.format("%02d.%1dkts %04drpm", sog/10, sog%10, rev);
                txtInfo1.setText(strInfo1);
                txtInfo2.setText(strInfo2);
                System.out.println("eng:" + eng + " rud:" + rud + " rev:" + rev + " sog:"
                        + sog + " cog:" + cog + " yaw:" + yaw + " dpt:"+ dpt);
            }
        }
    }
    private MessageWatcher watcher = null;

    UpdaterTask updaterTask = null;
    Timer updaterTimer = null;
    Handler updaterHandler = null;
    class UpdaterTask extends TimerTask{
        @Override
        public void run() {
            if(!watcher.isAlive()){
                watcher.start();
            }

            if(engState == nextEngState && rudState == nextRudState)
                return;

            try {
                CommandOuterClass.FltrInfo.Builder req_builder =
                        CommandOuterClass.FltrInfo.newBuilder();

                req_builder.setInstName("ui_manager");
                CommandOuterClass.FltrParInfo par_cmd = CommandOuterClass.FltrParInfo.newBuilder()
                        .setName("cmd_id")
                        .setVal("setctrlc")
                        .build();
                CommandOuterClass.FltrParInfo par_eng = CommandOuterClass.FltrParInfo.newBuilder()
                        .setName("ival0")
                        .setVal(String.valueOf(nextEngState.ordinal()))
                        .build();
                CommandOuterClass.FltrParInfo par_rud = CommandOuterClass.FltrParInfo.newBuilder()
                        .setName("ival1")
                        .setVal(String.valueOf(nextRudState.ordinal()))
                        .build();

                req_builder
                        .addPars(par_cmd)
                        .addPars(par_eng)
                        .addPars(par_rud);
                CommandOuterClass.Result res = stub_cmd.setFltrPar(req_builder.build());
                if(!res.getIsOk()) {
                     System.out.println(res.getMessage());
                }else {
                    engState = nextEngState;
                    rudState = nextRudState;
                    btnForward.setText(getEngStateIconString(getNextFEngState()));
                    btnBackward.setText(getEngStateIconString(getNextBEngState()));
                    btnPort.setText(getRudStateIconString(getNextPRudState()));
                    btnStarboard.setText(getRudStateIconString(getNextSRudState()));
                }
            }catch(StatusRuntimeException e)
            {
                e.printStackTrace();
            }

            updaterHandler.post(new Runnable() {
                public void run() {
                    System.out.println("UI Updating");
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eng = 127;
        engState = nextEngState = EngState.stp;

        rud = 127;
        rudState = nextRudState = RudState.mds;

        btnStarboard = (Button) findViewById(R.id.starboard);
        btnStarboard.setOnClickListener(this);

        btnPort = (Button) findViewById(R.id.port);
        btnPort.setOnClickListener(this);

        btnMidship = (Button) findViewById(R.id.midship);
        btnMidship.setOnClickListener(this);

        btnForward = (Button) findViewById(R.id.forward);
        btnForward.setOnClickListener(this);

        btnNeutral = (Button) findViewById(R.id.neutral);
        btnNeutral.setOnClickListener(this);

        btnBackward =(Button) findViewById(R.id.backward);
        btnBackward.setOnClickListener(this);

        skbRudder = (SeekBar) findViewById(R.id.rudder);
        skbEngine = (SeekBar) findViewById(R.id.engine);
        skbRudder.setMax(255);
        skbEngine.setMax(255);

        txtInfo1 = (TextView) findViewById(R.id.info1);
        txtInfo2 = (TextView) findViewById(R.id.info2);


        updaterTask = new UpdaterTask();
        updaterHandler = new Handler();
        updaterTimer = new Timer(true);
        updaterTimer.schedule(updaterTask,500,500);

        //        // Enables Always-on

        chan_cmd = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        chan_msg = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        stub_cmd = CommandService.CommandGrpc.newBlockingStub(chan_cmd);
        stub_msg = CommandService.CommandGrpc.newBlockingStub(chan_msg);
        watcher = new MessageWatcher();
        watcher.start();

        setAmbientEnabled();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Button btn = (Button) v;
        switch(btn.getId()) {
            case R.id.port:
                System.out.println("port");
                setNextRudState(btn.getId());
                break;
            case R.id.starboard:
                System.out.println("starboard");
                setNextRudState(btn.getId());
                break;
            case R.id.midship:
                System.out.println("midship");
                setNextRudState(btn.getId());
                break;
            case R.id.forward:
                System.out.println("forward");
                setNextEngState(btn.getId());
                break;
            case R.id.backward:
                System.out.println("backward");
                setNextEngState(btn.getId());
                break;
            case R.id.neutral:
                System.out.println("neutral");
                setNextEngState(btn.getId());
                break;
        }
    }

    public void setNextEngState(int id)
    {
        if(R.id.forward == id) {
            nextEngState = getNextFEngState();
            return;
        }

        if(R.id.backward == id) {
            nextEngState = getNextBEngState();
            return;
        }

        nextEngState = engState.stp;
    }

    public void setNextRudState(int id)
    {
        if(R.id.port == id){
            nextRudState = getNextPRudState();
            return;
        }

        if(R.id.starboard == id) {
            nextRudState = getNextSRudState();
            return;
        }

        nextRudState = RudState.mds;
    }

    protected RudState getNextSRudState()
    {
        switch(rudState){
            case mds:
                return RudState.s10;
            case p20:
                return RudState.p10;
            case hap:
                return RudState.p20;
            case s10:
                return RudState.s20;
            case s20:
            case has:
                return RudState.has;
            default:
                return RudState.mds;
        }
    }

    protected RudState getNextPRudState()
    {
        switch(rudState){
            case mds:
                return RudState.p10;
            case p10:
                return RudState.p20;
            case p20:
            case hap:
                return RudState.hap;
            case s20:
                return RudState.s10;
            case has:
                return RudState.s20;
            default:
                return RudState.mds;
        }
    }

    protected EngState getNextFEngState()
    {
        switch(engState){
            case stp:
                return engState.dsah;
            case dsah:
                return engState.slah;
            case slah:
                return engState.hfah;
            case hfah:
                return engState.flah;
            case flah:
                return engState.nf;
            case nf:
                return engState.nf;
            case dsas:
                return  engState.stp;
            case slas:
                return engState.dsas;
            case hfas:
                return engState.slas;
            case flas:
                return engState.hfas;
            default:
                return engState.stp;
        }
    }

    protected EngState getNextBEngState()
    {
        switch(engState){
            case stp:
                return engState.dsas;
            case dsas:
                return engState.slas;
            case slas:
                return engState.hfas;
            case hfas:
                return engState.flas;
            case flas:
                return engState.flas;
            case nf:
                return engState.flah;
            case dsah:
                return engState.stp;
            case slah:
                return engState.dsah;
            case hfah:
                return engState.slah;
            case flah:
                return engState.hfah;
            default:
                return engState.stp;
        }
    }

    protected String getEngStateIconString(EngState es)
    {
        switch(es){
            case stp:
                return "N";
            case dsas:
                return "B0";
            case slas:
                return "B1";
            case hfas:
                return "B2";
            case flas:
                return "B3";
            case nf:
                return "F4";
            case dsah:
                return "F0";
            case slah:
                return "F1";
            case hfah:
                return "F2";
            case flah:
                return "F3";
            default:
                return "-";
        }
    }

    protected String getRudStateIconString(RudState rs)
    {
        switch(rs){
            case mds:
                return "M";
            case p10:
                return "P0";
            case p20:
                return "P1";
            case hap:
                return "P2";
            case s10:
                return "S0";
            case s20:
                return "S1";
            case has:
                return "S2";
            default:
                return "-";
        }
    }
}
