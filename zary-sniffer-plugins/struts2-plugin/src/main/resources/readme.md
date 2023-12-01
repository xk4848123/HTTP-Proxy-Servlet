/*2.5.22---2.5-BETA1*/
import org.apache.struts2.dispatcher.filter.StrutsExecuteFilter;
import org.apache.struts2.dispatcher.filter.StrutsPrepareFilter;
import org.apache.struts2.dispatcher.filter.StrutsPrepareAndExecuteFilter;



/*2.3.37---2.1.6*/
import org.apache.struts2.dispatcher.ng.filter.StrutsExecuteFilter;
import org.apache.struts2.dispatcher.ng.filter.StrutsPrepareFilter;
import org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter;


/*2.1.2---2.0.5*/
//FilterDispatcher是struts2.0.x到2.1.2版本的核心过滤器.!
//StrutsPrepareAndExecuteFilter是自2.1.3开始就替代了FilterDispatcher的.!
import org.apache.struts2.dispatcher.FilterDispatcher;




配置servlet（/*2.5.22---2.5-BETA1*/）
1.
    <filter>
          <filter-name>struts2</filter-name>
          <filter-class>org.apache.struts2.dispatcher.filter.StrutsPrepareAndExecuteFilter</filter-class>
    </filter>
2.
    <filter>
        <filter-name>struts2-prepare</filter-name>
        <filter-class>org.apache.struts2.dispatcher.filter.StrutsPrepareFilter</filter-class>
    </filter>     
    <filter>
        <filter-name>struts2-execute</filter-name>
        <filter-class>org.apache.struts2.dispatcher.filter.StrutsExecuteFilter</filter-class>
    </filter>


配置servlet（/*2.3.37---2.1.6*/）

1.
    <filter>
          <filter-name>struts2</filter-name>
          <filter-class>org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter</filter-class>
      </filter>
2.
    <filter>
        <filter-name>struts2-prepare</filter-name>
        <filter-class>org.apache.struts2.dispatcher.ng.filter.StrutsPrepareFilter</filter-class>
    </filter>     
    <filter>
        <filter-name>struts2-execute</filter-name>
        <filter-class>org.apache.struts2.dispatcher.ng.filter.StrutsExecuteFilter</filter-class>
    </filter>
   
配置servlet（/*2.5.22---2.5-BETA1*/）

1.
    <filter>
        <filter-name>struts2</filter-name>
        <filter-class>org.apache.struts2.dispatcher.FilterDispatcher</filter-class>
    </filter>