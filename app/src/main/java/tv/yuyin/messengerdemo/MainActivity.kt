package tv.yuyin.messengerdemo

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import tv.yuyin.messengerdemo.logutil.LLog
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), View.OnClickListener {

    //服务端的Messenger，后面可以主动向服务端发送消息
    private var serverMessenger:Messenger? = null


    private lateinit var clientHandler: ClientHandler
    //客户端Messenger
    private lateinit var clientMessenger: Messenger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnHandShake.setOnClickListener(this)

        val handlerThread =  HandlerThread("ClientHandler")
        handlerThread.start()
        clientHandler = ClientHandler(handlerThread.looper)
        clientMessenger = Messenger(clientHandler)

        val intent = Intent(this,MessengerService::class.java)
        bindService(intent,serviceConnection,Service.BIND_AUTO_CREATE)
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            LLog.e("onServiceDisconnected. name=%s",name)
            serverMessenger = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            LLog.i("onServiceConnected. name=%s,service=%s",name,service)
            service?.let {
                //这里就获取到了服务端的Messenger，后面可以在任何时候向服务端发送消息
                serverMessenger = Messenger(it)
            }
        }
    }


    class ClientHandler(looper: Looper) : Handler(looper) {

       /* private var bufferedOutputStream:BufferedOutputStream?

        init {
            val outputStream = FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath+
                    File.separator+"test.pcm")
            bufferedOutputStream = BufferedOutputStream(outputStream,1024*512)
        }

        fun closeStream() {
            try {
                bufferedOutputStream?.close()
                bufferedOutputStream = null
            } catch (ex: Exception) {
                LLog.e("close output stream error",ex)
            }

        }*/

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MessengerService.MSG_WHAT_SERVER_CONNECT_RESPONSE -> {
                    val isSucceed =  msg.arg1 == 0
                    val text = msg.data?.getString("testArgKey")
                    LLog.d("客户端： 和服务端建立连接消息的回复=> isSucceed=%s,服务端回复内容=%s",isSucceed,text)
                }
                MessengerService.MSG_WHAT_PUBLISH_DATA -> {
                    val byteArray = msg.data?.getByteArray("testArgKey")
                    byteArray?.let {
                        val text = String(it, Charset.forName("UTF-8"))
                        LLog.d("客户端:收到消息：%s",text)
                    }
                }
/*                MessengerService.MSG_WHAT_PUBLISH_DATA2 -> {
                    val byteArray = msg.data?.getByteArray("testArgKey")
                    byteArray?.let {
                        bufferedOutputStream?.write(it)
                    }
                }*/
            }
        }

    }


    override fun onClick(clickedView: View) {
        when (clickedView) {
            btnHandShake -> {
                btnHandShake.isEnabled = false
                /**
                 * 客户端向服务端发送建立连接消息的目的是为了告诉服务端  客户端的Messenger
                 * 服务端拿到客户端的Messenger用于服务端在后续主动向客户端发消息
                 */
                serverMessenger?.let {
                    val message = Message.obtain(null,MessengerService.MSG_WHAT_CLIENT_CONNECT_SERVER)
                    val bundle = Bundle(1)
                    bundle.putString("testArgKey","客户端传递的Bundle String参数信息")
                    message.replyTo = clientMessenger
                    try {
                        it.send(message)
                        LLog.d("客户端向服务端发送建立连接消息成功")
                    } catch (ex: RemoteException) {
                        LLog.e("客户端向服务端发送建立连接消息失败",ex)
                    }
                }
            }
            else -> {

            }
        }
    }

    override fun onDestroy() {
        val message = Message.obtain(null, MessengerService.MSG_WHAT_DISCONNECT_SERVER)
        message.replyTo = clientMessenger
        serverMessenger?.send(message)

        unbindService(serviceConnection)

//        clientHandler.closeStream()
        super.onDestroy()
    }

}
