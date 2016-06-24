<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
  <script>
    redirect();
    function redirect() {
      var url = "<%= request.getParameter("url") %>";


      if (url.indexOf("join") !== -1) {
        var win = window.open(url, "_top");
      }else if(url.indexOf("content:") !== -1) {
       //window.close();
        if(window.location.href.indexOf("portal/site") === -1){
          window.history.back();
        }
        var win = window.open(url, "ltitool", "_blank");
      }else {
        var win = window.open(url, "_top");
      }
      win.focus();

    }
  </script>
  <title></title>
</head>
<body>

</body>
</html>
