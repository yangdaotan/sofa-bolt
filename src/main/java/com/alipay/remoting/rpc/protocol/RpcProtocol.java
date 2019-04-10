/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.CommandDecoder;
import com.alipay.remoting.CommandEncoder;
import com.alipay.remoting.CommandFactory;
import com.alipay.remoting.CommandHandler;
import com.alipay.remoting.HeartbeatTrigger;
import com.alipay.remoting.Protocol;
import com.alipay.remoting.rpc.RpcCommandFactory;

/**
 * Request command protocol for v1
 * 0     1     2           4           6           8          10           12          14         16
 * +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
 * |proto| type| cmdcode   |ver2 |   requestId           |codec|        timeout        |  classLen |
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |headerLen  | contentLen            |                             ... ...                       |
 * +-----------+-----------+-----------+                                                                                               +
 * |               className + header  + content  bytes                                            |
 * +                                                                                               +
 * |                               ... ...                                                         |
 * +-----------------------------------------------------------------------------------------------+
 *
 * proto: code for protocol
 * type: request/response/request oneway
 * cmdcode: code for remoting command
 * ver2:version for remoting command 不同版本对于的command处理方式不同
 * requestId: id of request
 * codec: code for codec
 * timeout：timeout for client from request to response
 * headerLen: length of header
 * contentLen: length of content
 *
 * Response command protocol for v1
 * 0     1     2     3     4           6           8          10           12          14         16
 * +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
 * |proto| type| cmdcode   |ver2 |   requestId           |codec|respstatus |  classLen |headerLen  |
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * | contentLen            |                  ... ...                                              |
 * +-----------------------+                                                                       +
 * |                         className + header  + content  bytes                                  |
 * +                                                                                               +
 * |                               ... ...                                                         |
 * +-----------------------------------------------------------------------------------------------+
 * respstatus: response status
 *
 * @author jiangping
 * @version $Id: RpcProtocol.java, v 0.1 2015-9-28 PM7:04:04 tao Exp $
 *
 * 一个协议就是对传输内容以及排列方式的约定，可以让client和server进行解析和应答，即：
 *      约定request command、response command。
 *
 *    - 需要明确：
 *      1、command的编解码commandEncoder、commandecoder
 *      2、各种command code的语义以及相应的处理，commandHandler负责command code -> processor
 *      3、协议对command的管理，由commandFactory负责
 *      4、心跳处理 HeartbeatTrigger
 *      5、编码需要序列化，解码需要反序列化，因此需要codec来表示序列化方式
 *
 *    - 协议内容
 *      1、由header和body组成，一般可以有多个<header, body>
 *      2、对于request、response可以设计一个通用的协议内容，也可以分开设计，有各种的header和body
 *      3、为了扩展，会设计字段协议版本号version，这边是Protocol code
 *
 */
public class RpcProtocol implements Protocol {
    public static final byte PROTOCOL_CODE       = (byte) 1;
    private static final int REQUEST_HEADER_LEN  = 22;
    private static final int RESPONSE_HEADER_LEN = 20;
    private CommandEncoder   encoder;
    private CommandDecoder   decoder;
    private HeartbeatTrigger heartbeatTrigger;
    private CommandHandler   commandHandler;
    private CommandFactory   commandFactory;

    public RpcProtocol() {
        this.encoder = new RpcCommandEncoder();
        this.decoder = new RpcCommandDecoder();
        this.commandFactory = new RpcCommandFactory();
        this.heartbeatTrigger = new RpcHeartbeatTrigger(this.commandFactory);
        this.commandHandler = new RpcCommandHandler(this.commandFactory);
    }

    /**
     * Get the length of request header.
     */
    public static int getRequestHeaderLength() {
        return RpcProtocol.REQUEST_HEADER_LEN;
    }

    /**
     * Get the length of response header.
     */
    public static int getResponseHeaderLength() {
        return RpcProtocol.RESPONSE_HEADER_LEN;
    }

    @Override
    public CommandEncoder getEncoder() {
        return this.encoder;
    }

    @Override
    public CommandDecoder getDecoder() {
        return this.decoder;
    }

    @Override
    public HeartbeatTrigger getHeartbeatTrigger() {
        return this.heartbeatTrigger;
    }

    @Override
    public CommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    @Override
    public CommandFactory getCommandFactory() {
        return this.commandFactory;
    }
}
