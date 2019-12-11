/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2019 The eXist Project
 * http://exist-db.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exist.xquery.functions.fn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.dom.QName;
import org.exist.xquery.*;
import org.exist.xquery.value.*;

import static org.exist.xquery.FunctionDSL.*;

/**
 * Implements fn:format-integer as per W3C XPath and XQuery Functions and Operators 3.1
 *
 * fn:format-integer($value as xs:integer?, $picture as xs:string) as xs:string
 * fn:format-integer($value as xs:integer?, $picture as xs:string, $lang as xs:string?) as xs:string
 *
 * @author <a href="mailto:from-github-existdb@agh2342.de">Adrian Hamm</a>
 */
public class FunFormatInteger extends BasicFunction {

    private final static Logger logger = LogManager.getLogger();

    private static final String FS_FORMAT_INTEGER_NAME = "format-integer";
    private static final FunctionParameterSequenceType FS_FORMAT_INTEGER_OPT_PARAM_VALUE = optParam("value", Type.INTEGER, "The integer value");
    private static final FunctionParameterSequenceType FS_FORMAT_INTEGER_PARAM_PICTURE = param("picture", Type.STRING, "The picture string");
    private static final FunctionParameterSequenceType FS_FORMAT_INTEGER_OPT_PARAM_LANG = optParam("lang", Type.STRING, "The output language");
    static final FunctionSignature[] FS_FORMAT_INTEGER = functionSignatures(
            new QName(FS_FORMAT_INTEGER_NAME, Function.BUILTIN_FUNCTION_NS),
            "Formats an integer according to a given picture string, using the conventions of a given natural language if specified.",
            returns(Type.STRING, "The formatted string representation of the supplied integer"),
            arities(
                    arity(FS_FORMAT_INTEGER_OPT_PARAM_VALUE, FS_FORMAT_INTEGER_PARAM_PICTURE),
                    arity(FS_FORMAT_INTEGER_OPT_PARAM_VALUE, FS_FORMAT_INTEGER_PARAM_PICTURE, FS_FORMAT_INTEGER_OPT_PARAM_LANG)
            )
    );

    public FunFormatInteger(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    public Sequence eval(final Sequence[] args, final Sequence contextSequence) throws XPathException {
        final Sequence result;
        final Sequence value = (getArgumentCount() > 0) ? args[0] : null;
        final String picture = (getArgumentCount() > 1) ? args[1].toString() : null;

        if (value == null) {

        } else if (value.isEmpty()) {
//            result = Sequence.EMPTY_SEQUENCE;
            result = new ValueSequence();
            result.add(new StringValue("\"\""));
            return result;
        }

        final Item item = value.itemAt(0);
/*
        if (item.getType() != Type.DOCUMENT) {
            result = Sequence.EMPTY_SEQUENCE;
            return result;
        }
*/
        result = new ValueSequence();
//        final NodeValue nodeValue = (NodeValue) item;
        result.add(new StringValue("\"" + value.toString() + "\""));
        return result;
    }
}
