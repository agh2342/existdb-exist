/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2001-2018 The eXist Project
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.exist.dom.persistent;

import org.exist.xquery.value.SequenceIterator;
import org.junit.Test;
import org.w3c.dom.Node;

import static org.junit.Assert.assertEquals;

public class NodeProxyTest {

    @Test
    public void iterate_loop() {
        final NodeProxy mockNodeProxy = new NodeProxy(null, null, Node.ELEMENT_NODE, -1);

        final SequenceIterator it = mockNodeProxy.iterate();
        int count = 0;
        while (it.hasNext()) {
            it.nextItem();
            count++;
        }

        assertEquals(1, count);
    }

    @Test
    public void iterate_skip_loop() {
        final NodeProxy mockNodeProxy = new NodeProxy(null, null, Node.ELEMENT_NODE, -1);
        final SequenceIterator it = mockNodeProxy.iterate();

        assertEquals(1, it.skippable());

        assertEquals(1, it.skip(10));

        assertEquals(0, it.skippable());

        int count = 0;
        while (it.hasNext()) {
            it.nextItem();
            count++;
        }

        assertEquals(0, count);
    }

    @Test
    public void iterate_loop_skip_loop() {
        final NodeProxy mockNodeProxy = new NodeProxy(null, null, Node.ELEMENT_NODE, -1);
        final SequenceIterator it = mockNodeProxy.iterate();

        int len = 20;
        int count = 0;
        for (int i = 0; it.hasNext() && i < len; i++) {
            it.nextItem();
            count++;
        }
        assertEquals(1, count);

        assertEquals(0, it.skippable());

        assertEquals(0, it.skip(10));

        assertEquals(0, it.skippable());

        count = 0;
        while (it.hasNext()) {
            it.nextItem();
            count++;
        }

        assertEquals(0, count);
    }
}
