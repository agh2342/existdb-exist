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

import com.fasterxml.jackson.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.dom.QName;
import org.exist.xquery.*;
import org.exist.xquery.functions.map.MapType;
import org.exist.xquery.value.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import static org.exist.xquery.FunctionDSL.*;

/**
 * fn:default-language() as xs:language
 *
 * @author <a href="mailto:from-github-existdb@agh2342.de">Adrian Hamm</a>
 */
public class FunDefaultLanguage extends Function {

    private final static Logger logger = LogManager.getLogger();

    private static final String FS_DEFAULT_LANGUAGE_NAME = "default-language";
    static final FunctionSignature FS_DEFAULT_LANGUAGE = functionSignature(
            new QName(FS_DEFAULT_LANGUAGE_NAME, Function.BUILTIN_FUNCTION_NS),
            "Returns the value of the default language property from the dynamic context.",
            returns(Type.LANGUAGE, "The value of the default language")
    );

    public FunDefaultLanguage(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    public Sequence eval(final Sequence contextSequence, final Item contextItem) throws XPathException {
        final Sequence result;

        if (contextItem == null) {
            //context missing, so throw Exception
            throw new XPathException(ErrorCodes.FOER0000, "No default language in dynamic context.");
        } else {
            result = new ValueSequence();
            result.add(new StringValue(""));
            //dynamic context does not contain defaultLanguage, so throw Exception
            throw new XPathException(ErrorCodes.FOER0000, "No default language in dynamic context.");
        }

//        return result;
    }
}
