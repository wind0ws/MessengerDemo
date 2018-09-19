package tv.yuyin.messengerdemo

import android.app.Service
import android.content.Intent
import android.os.*
import tv.yuyin.messengerdemo.logutil.LLog
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MessengerService : Service() {

    companion object {
        /**
         * 客户端发送消息通知服务端要建立连接
         * （服务端把客户端的Messenger添加到clientMessengers中）
         */
        const val MSG_WHAT_CLIENT_CONNECT_SERVER = 1

        /**
         * 客户端发送消息通知服务端要解除连接
         * （服务端把客户端的Messenger从clientMessengers中移除掉）
         */
        const val MSG_WHAT_DISCONNECT_SERVER = 2

        /**
         * 服务端回复 客户端的建立连接消息
         * @see MSG_WHAT_CLIENT_CONNECT_SERVER
         */
        const val MSG_WHAT_SERVER_CONNECT_RESPONSE = 3


        /**
         * 发布数据消息
         */
        const val MSG_WHAT_PUBLISH_DATA = 4

        const val MSG_WHAT_PUBLISH_DATA2 = 5

    }

    private val clientMessengers:HashSet<Messenger> = HashSet()

    class MessengerHandler(private val clientMessengers:HashSet<Messenger>,looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            msg?.let {
                when (msg.what) {
                    MSG_WHAT_CLIENT_CONNECT_SERVER ->{
                        LLog.d("服务端收到客户端的建立连接消息")
                        //握手的目的是为了获取客户端的Messenger
                        //获取到客户端Messenger，后面可以主动向客户端发送消息
                        val clientMessenger = msg.replyTo
                        val isSucceed = clientMessengers.add(clientMessenger)

                        val message = Message.obtain(null, MSG_WHAT_SERVER_CONNECT_RESPONSE,
                                if (isSucceed) 0 else 1,0)
                        val bundle = Bundle(1)
                        bundle.putString("testArgKey","我是服务端，我获取到你（Client）的Messenger了哦，后面我可以主动向你发消息了")
                        message.data = bundle
                        try {
                            clientMessenger.send(message)
                        } catch (ex:RemoteException) {
                            LLog.e("消息发送失败",ex)
                        }
                    }
                    MSG_WHAT_DISCONNECT_SERVER ->{
                        LLog.d("服务端收到客户端的断连消息")
                        val clientMessenger = msg.replyTo
                        val isSucceed = clientMessengers.remove(clientMessenger)
                        LLog.d("服务端解除连接。isSucceed = %s",isSucceed)
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private lateinit var serverMessengerHandler: MessengerHandler
    private lateinit var executorService: ExecutorService
    private var isContinuePullOutData = true

    override fun onBind(intent: Intent): IBinder {
        return Messenger(serverMessengerHandler).binder
    }

    override fun onCreate() {
        LLog.i("onCreate")
        super.onCreate()

        val messengerHandlerThread = HandlerThread("MessengerService.HandlerThread")
        messengerHandlerThread.start()
        serverMessengerHandler = MessengerHandler(clientMessengers,messengerHandlerThread.looper)

        executorService = Executors.newSingleThreadExecutor()
        executorService.submit(pulloutDataRunnable)
    }

    private val pulloutDataRunnable = Runnable {
        var counter = 0
//        var isReadFileFinished = false
        while (isContinuePullOutData) {
            if (clientMessengers.size == 0) {
                SystemClock.sleep(200)
                continue
            }

/*            if (!isReadFileFinished) {
                val byteBuffer = ByteArray(4096)
                val inputStream = FileInputStream("/sdcard/FarVid/regVoice/origin_DD0.pcm")
                var readCount:Int
                do {
                    readCount = inputStream.read(byteBuffer)
                    if (readCount > 0) {
                        val message = Message.obtain(null, MSG_WHAT_PUBLISH_DATA2)
                        val bundle = Bundle(1)
                        bundle.putByteArray("testArgKey",Arrays.copyOfRange(byteBuffer,0,readCount))
                        message.data = bundle

                        clientMessengers.first().send(message)
                    }
                }while (readCount>0)
                inputStream.close()
                LLog.w("读取文件完成")
                isReadFileFinished = true
            }*/


            val dataBytes = "This is Dummy data at ${counter++}".toByteArray(Charset.forName("UTF-8"))
            val remoteExMessengers = HashSet<Messenger>()
            for (messenger in clientMessengers) {
                try {
                    val message = Message.obtain(null, MSG_WHAT_PUBLISH_DATA)
                    val bundle = Bundle(1)
                    bundle.putByteArray("testArgKey",dataBytes)
                    message.data = bundle

                    messenger.send(message)
                } catch (ex: RemoteException) {
                    LLog.e("发送data消息出错",ex)
                    remoteExMessengers.add(messenger)
                }
            }
            clientMessengers.removeAll(remoteExMessengers)
            SystemClock.sleep(500)
        }
    }


    override fun onDestroy() {
        isContinuePullOutData = false
        LLog.i("onDestroy")
        executorService.shutdownNow()
        super.onDestroy()
    }
}
