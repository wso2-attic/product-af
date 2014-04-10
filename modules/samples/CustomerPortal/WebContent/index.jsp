<%@page import="com.abc.sales.customer.Customer" %>
<%@page import="com.abc.sales.customer.dao.CustomerDAO" %>
<%@ page import="java.util.*" %>


<%
    CustomerDAO dao = new CustomerDAO();
    Customer[] customers = dao.getCustomers();

    
   /* List<Customer> customers1 = new ArrayList<Customer>();
    Customer customer = new Customer("Sun Travels","Travel","Europe");
    customers1.add(customer);
     customer = new Customer("My Home","Retail","USA");
    customers1.add(customer);
     customer = new Customer("Kids Unlimited","Retail","USA");
    customers1.add(customer);
     customer = new Customer("Herbal Spa","Health","USA");
    customers1.add(customer);
     customer = new Customer("Cologne Mart","Retail","Midle East");
    customers1.add(customer);
     customer = new Customer("Bank of Ceylon","Banking","Asia");
    customers1.add(customer);

    Customer[] customers = customers1.toArray(new Customer[customers1.size()]);*/
    
%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="//ajax.googleapis.com/ajax/libs/dojo/1.8.3/dojo/dojo.js"></script>
        <title>Customers portal</title>
        <style type="text/css">
            body{
                font-family: "Helvetica Neue","Helvetica";
                font-size: 100%;
            }
        table.data{
            width:100%;
            border:1px solid #A36685;
            border-collapse:collapse;
            -webkit-border-radius: 4px;
            -moz-border-radius: 4px;
            border-radius: 4px;
            -webkit-box-shadow:  2px 2px 3px 3px rgba(0, 0, 0, 0.2);
            -moz-box-shadow:  2px 2px 3px 3px rgba(0, 0, 0, 0.2);
            box-shadow:  2px 2px 3px 3px rgba(0, 0, 0, 0.2);
        }
        table.data tr th{
            padding: 8px;
            background-color: #660033;
            color:#ffffff;
            font-weight: bold;

        }
        table.data tr td{
            padding: 8px;
            border:1px solid #A36685;
        }
            div.block{
                float:left;
                width:400px;
                heigth:380px;
                overflow: hidden;
                text-align: center;
            }
            h2{
                color: #0066FF;
            }
            h6{
                margin: 0px;
                padding: 0px;
                color: #0052CC;
            }

            .dijitTooltip {
                position: absolute;
                z-index: 2000;
                display: block;

                left: 0;
                top: -10000px;
                overflow: visible;
            }
            .dijitTooltipContainer {
                border: solid black 1px;
                background: #b8b5b5;
                color: black;
                font-size: small;
                padding: 3px 20px 3px 20px;
            }
            .dijitTooltipFocusNode {
                padding: 2px 2px 2px 2px;
            }
            .dijitTooltipConnector {
                position: absolute;
            }
            .dijit_a11y .dijitTooltipConnector {
                display: none;
            }
            .dijitTooltipData {
                display:none;
            }
        </style>

    </head>
     <body>
     <div class="block">
        <h2>Customers</h2>   
        <table border="1" class="data">
        <tr><th>Name</th><th>Business</th><th>Region</th></tr>
        <%
            HashMap<String,Integer> categoryMap=new HashMap<String, Integer>();
            HashMap<String,Integer> regionMap=new HashMap<String, Integer>();

            for (Customer cust : customers) {
        		out.print("<tr><td>" + cust.getName() + "</td><td>"
        				+ cust.getCategory() + "</td><td>" + cust.getRegion()
        				+ "</td></tr>");
                if(categoryMap.get(cust.getCategory())!=null){
                    categoryMap.put(cust.getCategory(),categoryMap.get(cust.getCategory())+1);
                }else{
                    categoryMap.put(cust.getCategory(),1);
                }
                if(regionMap.get(cust.getRegion())!=null){
                    regionMap.put(cust.getRegion(),regionMap.get(cust.getRegion())+1);
                }else{
                    regionMap.put(cust.getRegion(), 1);
                }
        	}
            String values="" ;
            String regionValues="";
            Set<String> keys=categoryMap.keySet();
            for(String key:keys){
                //out.print(key+" : "+categoryMap.get(key));
                values=values+categoryMap.get(key)+",";
            }
            values=values+"0";
            Set<String> keysR=regionMap.keySet();
            for(String key:keysR){
                //out.print(key+" : "+categoryMap.get(key));
                regionValues=regionValues+regionMap.get(key)+",";
            }
            regionValues=regionValues+"0";
        %>
        </table>
     </div>
     <div class="block">
        <script>
            require([
                // Require the basic chart class
                "dojox/charting/Chart",

                // Require the theme of our choosing
                "dojox/charting/themes/Wetland",

                // Charting plugins:

                // 	We want to plot a Pie chart
                "dojox/charting/plot2d/Pie",

                // Retrieve the Legend, Tooltip, and MoveSlice classes
                "dojox/charting/action2d/Tooltip",
                "dojox/charting/action2d/MoveSlice",

                //	We want to use Markers
                "dojox/charting/plot2d/Markers",

                //	We'll use default x/y axes
                "dojox/charting/axis2d/Default",

                // Wait until the DOM is ready
                "dojo/domReady!"
            ], function(Chart, theme, Pie, Tooltip, MoveSlice) {

                // Define the data
                var chartData = [<%=values%>];

                // Create the chart within it's "holding" node
                var chart = new Chart("chartNode");

                // Set the theme
                chart.setTheme(theme);

                // Add the only/default plot
                chart.addPlot("default", {
                    type: Pie,
                    markers: true,
                    radius:170
                });

                // Add axes
                chart.addAxis("x");
                chart.addAxis("y", { min: 5000, max: 30000, vertical: true, fixLower: "major", fixUpper: "major" });

                // Add the series of data
                chart.addSeries("Categories",chartData);

                // Create the tooltip
                var tip = new Tooltip(chart,"default");

                // Create the slice mover
                var mag = new MoveSlice(chart,"default");

                // Render the chart!
                chart.render();

            });
        </script>

        <div id="chartNode" style="width:400px;height:380px;"></div>
         <h6>Customers by Category</h6>
     </div>
     <div class="block">
         <script>
             require([
                 // Require the basic chart class
                 "dojox/charting/Chart",

                 // Require the theme of our choosing
                 "dojox/charting/themes/PurpleRain",

                 // Charting plugins:

                 // 	We want to plot a Pie chart
                 "dojox/charting/plot2d/Pie",

                 // Retrieve the Legend, Tooltip, and MoveSlice classes
                 "dojox/charting/action2d/Tooltip",
                 "dojox/charting/action2d/MoveSlice",

                 //	We want to use Markers
                 "dojox/charting/plot2d/Markers",

                 //	We'll use default x/y axes
                 "dojox/charting/axis2d/Default",

                 // Wait until the DOM is ready
                 "dojo/domReady!"
             ], function(Chart, theme, Pie, Tooltip, MoveSlice) {

                 // Define the data
                 var chartData = [<%=regionValues%>];

                 // Create the chart within it's "holding" node
                 var chart = new Chart("chartNodeRegion");

                 // Set the theme
                 chart.setTheme(theme);

                 // Add the only/default plot
                 chart.addPlot("default", {
                     type: Pie,
                     markers: true,
                     radius:170
                 });

                 // Add axes
                 chart.addAxis("x");
                 chart.addAxis("y", { min: 5000, max: 30000, vertical: true, fixLower: "major", fixUpper: "major" });

                 // Add the series of data
                 chart.addSeries("Categories",chartData);

                 // Create the tooltip
                 var tip = new Tooltip(chart,"default");

                 // Create the slice mover
                 var mag = new MoveSlice(chart,"default");

                 // Render the chart!
                 chart.render();

             });
         </script>

         <div id="chartNodeRegion" style="width:400px;height:380px;"></div>
         <h6>Customers by Region</h6>
     </div>
    </body>
</html>
