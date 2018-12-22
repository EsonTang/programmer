package com.prize.test;

import com.prize.app.util.safe.XXTEAUtil;

import java.net.URLDecoder;


/**
 * longbaoxiu
 * 2017/8/29.11:20
 */

public class Test {
    public static void main(String args[]) {
        String p=XXTEAUtil.getResponseDecrypt("ss5qdq94fuvEgEiYsvnYbjnhgd0%2FH30UEQCIaowYhj80P8OZV8m" +
                "emEaa2YksPZgt6%2F5zdTTlxQfg%2BSY3EPhMZfLPjfviz1OqHorPM2ASqP9k%2BzFon2%2FHILwD3VoQFCJ%2" +
                "FEMlBOh0y50MvfqNFFhqCqzwZgrlT972KeczVBvcw97wk%2F8xz86ECgG1aDfQDCqwLbYBs3N39pu2uq1691Y514" +
                "waReR5KjuYWoXH0EcZHBmPWR1yW%2FuStS%2B6KH6fGD5%2BUobR2XaigSv%2FuKnA5UlIxy0%2BeY%2F%2BCgq3h" +
                "1skImMAuf9IAnGGjmtJC3kKc6COH2RYWzHtMDvsvQ96ZkCdiGa8cRlK2magIIWS78bFXcPEB6j6dMgAVOloc4Qs6JD" +
                "Ezo9iDTzextlHAGE4aKzvggHyEFN%2FQaxFXT6MyBQfRvQPx5nhmKHwZCgvWLZAcBd33HyZuxfk7kpJcfTwluIzRm8p" +
                "qpgndLki1GevuLcg06XgPrvr6aPXTL5wCJkrFTINB3I4lcoA8hNKaAjTaPXcWNSB%2FsKn8sVeWVFt0bQGKO92oznx" +
                "txDbErpmTEIa3FKYTzeUIQ6deI2HGoUrXb6%2B0U4xRMkMAUA0Jc0XhK9WtisTjoDB5z9O4LIp4l4EdKRSwqADChy3" +
                "7H74LqAjuA%2B7O%2Frpwzsf0BZSzczvEdutmpB8Q1xK3saA%2BY2E4OP10389HqXXG3ae%2BEq2yu%2FETV9WFECU" +
                "zU7Ytq0X%2Fy650T5pQIhF41jySybAm4D5go5xlgBXpL1rFbjX8Z7ltbIUWbFqa4J9UdEnJFulcASHBIaYqwDwqGWm" +
                "61%2FhlA2tTOKr43fik%2F%2F9NPFzt4RT6OzvHwE9o3Iha2Y3DZPCq973vmd0l6rTTYTJo2PHkpbMSKRptpZiK%2B" +
                "Gp%2FrEdG9JDXEhG2yFaFMc0AsVOkq9INyHCCvZWVJVFGXAKTlQeg9keUh0gmb74uWaJ7tkCKLghnyQuzgf%2Bs%2" +
                "BdPbUZN%2FwAIqMcK0si9AHD%2FmzuL27HTJfZbIC1v6c0MubnGzEStq53bBZ2tvD8AC5dZmzlLPhwoRMEBo4s68k" +
                "JlFk7yrN9VcQoL5LC0Dyr1FWfNykg%2BXPo5F5tECWzPW2XhY0H4bqeADZlT12Otw%2FFER8DbP%2BuoQi9c9PxNjN" +
                "Yp8zubFi06%2F4H55OTEiK0lb%2BXQpmeT4kZuq91t3JlHVOHdXn5njCl%2F38qDc%2FheCGR03ESdLXPqA5rCbgj" +
                "zvrxLuL29l%2FC8GyOI%2FlubyXt5x1werP3xy9k576484MUNM7YYnWI4JtGVQjSqvCMGIh5A6LzBfPs%2FO3hYuLi" +
                "a5DHoLqqjx7fBws6fypuGwjtOAo1Je8FZjpTX3oSu4Zq8Aobm15Qy0G%2BmO4KWqf1y4jBb%2FpKQlcloM797KFmjS" +
                "ThyW3ahj3803wp8JUH6fF2wUfC5QxMh1soFOlSB%2FNXgz2%2BuoqVY2Zm0gMoM6xTZol0jMbNLvgCgd5u6lKa%2FGW" +
                "1n7H7lzovUuAfvygoB4auw88MTTDqGgEzPZBVmma6Fkf%2BQSmCX%2B8kCp9DICz%2BR" +
                "b01tPCl%2BJtuO59UDHVTUvtZL7E1Klh4NnAr8TMCzvHic4e9sY0T%2BOLVnsw9yz9EQhoUKTIlEwygA8Pnp05YAr1f6LxPvzbGyK90BfgwkA");
        System.out.println("解密后："+URLDecoder.decode(p) );

    }
}
