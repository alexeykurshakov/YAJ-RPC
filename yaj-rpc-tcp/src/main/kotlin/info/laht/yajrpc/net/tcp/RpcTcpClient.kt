/*
 * The MIT License
 *
 * Copyright 2018 Lars Ivar Hatledal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package info.laht.yajrpc.net.tcp

import info.laht.yajrpc.net.AbstractRpcClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.Charset

open class RpcTcpClient(
        host: String,
        port: Int
) : AbstractRpcClient() {

    private val socket: Socket = Socket(host, port)
    private val `in` = socket.getInputStream().buffered()
    private val out = socket.getOutputStream().buffered()

    init {
        start()
    }

    private fun start() {
        Thread {

            val lenBuf = ByteArray(4)
            try {
                while (true) {

                    `in`.read(lenBuf)
                    val len = ByteBuffer.wrap(lenBuf).int
                    val msg = ByteArray(len).also {
                        `in`.read(it, 0, len)
                    }.toString(Charset.defaultCharset())

                    messageReceived(msg)

                }
            } catch (ex: IOException) {
                LOG.trace("Caught exception", ex)
            }

        }.start()
    }

    override fun close() {
        super.close()
        socket.close()
    }

    override fun internalWrite(msg: String) {

        val bytes = msg.toByteArray()
        val len = ByteBuffer.allocate(4).putInt(bytes.size).array()

        out.write(len)
        out.write(bytes)
        out.flush()
    }

    private companion object {
        val LOG: Logger = LoggerFactory.getLogger(RpcTcpClient::class.java)
    }

}