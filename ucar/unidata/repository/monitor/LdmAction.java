/**
 *
 * Copyright 1997-2005 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundastion,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */





package ucar.unidata.repository.monitor;


import ucar.unidata.repository.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.30 $
 */
public class LdmAction extends MonitorAction {

    public static final String []FEED_TYPES = {
        "PPS",
        "DDS",
        "HDS",
        "IDS",
        "SPARE",
        "UNIWISC",
        "PCWS",
        "FSL2",
        "FSL3",
        "FSL4",
        "FSL5",
        "GPSSRC",
        "CONDUIT",
        "FNEXRAD",
        "LIGHTNING",
        "WSI",
        "DIFAX",
        "FAA604",
        "GPS",
        "FNMOC",
        "GEM",
        "NIMAGE",
        "NTEXT",
        "NGRID",
        "NPOINT",
        "NGRAPH",
        "NOTHER",
        "NEXRAD3",
        "NEXRAD2",
        "NXRDSRC",
        "EXP",
        "ANY",
        "NONE",
        "DDPLUS",
        "WMO",
        "UNIDATA",
        "FSL",
        "NMC",
        "NPORT",
    };

    /** _more_          */
    private static final String ARG_PQINSERT = "pqinsert";

    /** _more_          */
    private static final String ARG_FEED = "feed";

    /** _more_          */
    private static final String ARG_QUEUE = "queue";

    /** _more_          */
    private static final String ARG_PRODUCTID = "productid";

    /** _more_          */
    private String queue="";

    /** _more_          */
    private String pqinsert="";

    /** _more_          */
    private String feed = "SPARE";

    private String productId="";


    /**
     * _more_
     */
    public LdmAction() {}

    /**
     * _more_
     *
     * @param id _more_
     */
    public LdmAction(String id) {
        super(id);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getActionName() {
        return "LDM Action";
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getSummary() {
        return "Inject into LDM";
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param monitor _more_
     */
    public void applyEditForm(Request request, EntryMonitor monitor) {
        super.applyEditForm(request, monitor);
        this.pqinsert = request.getString(getArgId(ARG_PQINSERT), "");
        this.feed     = request.getString(getArgId(ARG_FEED), "");
        this.queue    = request.getString(getArgId(ARG_QUEUE), "");
        this.productId    = request.getString(getArgId(ARG_PRODUCTID), "");
    }


    /**
     * _more_
     *
     * @param monitor _more_
     * @param sb _more_
     */
    public void addToEditForm(EntryMonitor monitor, StringBuffer sb) {
        sb.append(HtmlUtil.formTable());
        sb.append(HtmlUtil.colspan("LDM Action", 2));
        sb.append(HtmlUtil.formEntry("Path to pqinsert:",
                                     HtmlUtil.input(getArgId(ARG_PQINSERT),
                                         pqinsert, HtmlUtil.SIZE_60)));
        sb.append(HtmlUtil.formEntry("Queue Location:",
                                     HtmlUtil.input(getArgId(ARG_QUEUE),
                                         queue, HtmlUtil.SIZE_60)));
        sb.append(HtmlUtil.formEntry("Feed:",
                                     HtmlUtil.select(getArgId(ARG_FEED), Misc.toList(FEED_TYPES),feed)));
        String tooltip = "macros: ${fromday}  ${frommonth} ${fromyear} ${frommonthname}  <br>" +
            "${today}  ${tomonth} ${toyear} ${tomonthname} <br> " +
            "${filename}  ${fileextension}";
        sb.append(HtmlUtil.formEntry("Product ID:",
                                     HtmlUtil.input(getArgId(ARG_PRODUCTID), productId,
                                         HtmlUtil.SIZE_60+
                                                    HtmlUtil.title(tooltip))));

        sb.append(HtmlUtil.formTableClose());
    }


    /**
     * _more_
     *
     *
     * @param monitor _more_
     * @param entry _more_
     */
    protected void entryMatched(EntryMonitor monitor, Entry entry) {
        try {
            Resource resource = entry.getResource();
            if(!resource.isFile()) {
                System.err.println ("Entry is not a file:" + entry);
                return;
            }
            String id = productId.trim();
            if(id.length()>0) {
                id  = " -p \"" +  monitor.getRepository().getEntryManager().replaceMacros(entry, productId) +"\" ";
            }
            String command = pqinsert+" " +id +" -f " + feed +" -q " + queue +" " + resource.getPath();
            System.err.println("Executing:" + command);
            Process process = Runtime.getRuntime().exec(command);
            int result = process.waitFor();
            if(result==0) {
                System.err.println("Success");
            } else {
                System.err.println("Failed");
                try {
                    InputStream is = process.getErrorStream();
                    byte[] bytes = IOUtil.readBytes(is);
                    System.err.println("Error:" + new String(bytes));
                } catch(Exception noop) {}
            }
            
        } catch (Exception exc) {
            monitor.handleError("Error posting to LDM", exc);
        }
    }


    /**
     * Set the Pqinsert property.
     *
     * @param value The new value for Pqinsert
     */
    public void setPqinsert(String value) {
        pqinsert = value;
    }

    /**
     * Get the Pqinsert property.
     *
     * @return The Pqinsert
     */
    public String getPqinsert() {
        return pqinsert;
    }



    /**
     *  Set the Feed property.
     *
     *  @param value The new value for Feed
     */
    public void setFeed(String value) {
        feed = value;
    }

    /**
     *  Get the Feed property.
     *
     *  @return The Feed
     */
    public String getFeed() {
        return feed;
    }

    /**
     * Set the Queue property.
     *
     * @param value The new value for Queue
     */
    public void setQueue(String value) {
        queue = value;
    }

    /**
     * Get the Queue property.
     *
     * @return The Queue
     */
    public String getQueue() {
        return queue;
    }

    /**
       Set the ProductId property.

       @param value The new value for ProductId
    **/
    public void setProductId (String value) {
	productId = value;
    }

    /**
       Get the ProductId property.

       @return The ProductId
    **/
    public String getProductId () {
	return productId;
    }



}

