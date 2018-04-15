/**
 * Copyright 2009-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.parsing;

/**
 * @author Clinton Begin
 */
public class GenericTokenParser {

    private final String openToken;
    private final String closeToken;
    private final TokenHandler handler;

    /**
     *
     * @param openToken 占位符开始
     * @param closeToken 占位符结束
     * @param handler 解析逻辑
     */
    public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.handler = handler;
    }

    public String parse(String text) {
        final StringBuilder builder = new StringBuilder();
        final StringBuilder expression = new StringBuilder();
        if (text == null || text.length() < 1) {
            return "";
        }
        char[] src = text.toCharArray();
        int offset = 0;
        // search open token
        int start = text.indexOf(openToken, offset);

        while (start > -1) {
            //开始标记是转义的就算了 去掉\
            if (start > 0 && src[start - 1] == '\\') {
                // this open token is escaped. remove the backslash and continue.
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
                start = text.indexOf(openToken, offset);
                continue;
            }

            // found open token. let's search close token.
            expression.setLength(0);//清零
            builder.append(src, offset, start - offset);//前面一部分
            offset = start + openToken.length();
            int end = text.indexOf(closeToken, offset);
            while (end > -1) {
                if (end > offset && src[end - 1] == '\\') {
                    // this close token is escaped. remove the backslash and continue.
                    expression.append(src, offset, end - offset - 1).append(closeToken);
                    offset = end + closeToken.length();
                    end = text.indexOf(closeToken, offset);
                    continue;
                }

                expression.append(src, offset, end - offset);
                break;
            }

            if (end == -1) {
                // 关闭标记没找到
                builder.append(src, start, src.length - start);
                return builder.toString();
            }
            //todo 替换成解析出来的一部分 把整个${XXX}给TokenHandler 静态怎么解析由TokenHandler不同的实现来管
            builder.append(handler.handleToken(expression.toString()));
            offset = end + closeToken.length();



            start = text.indexOf(openToken, offset);
        }//while end

        //剩下的连回去
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }

        return builder.toString();
    }
}
