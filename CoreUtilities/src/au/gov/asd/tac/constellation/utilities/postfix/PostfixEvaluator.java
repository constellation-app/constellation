/*
 * Copyright 2010-2019 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.utilities.postfix;

import java.util.Stack;

/**
 * Found at
 * https://www.geeksforgeeks.org/stack-set-4-evaluation-postfix-expression/
 *
 * @author aldebaran30701
 */
public class PostfixEvaluator {

    // Method to evaluate value of a postfix expression
    public static String evaluatePostfix(String exp) {
        //create a stack
        Stack<String> stack = new Stack<>();
        //System.out.println(exp);
        String[] expr = exp.split(" ");
        //System.out.println("expr count before pop: " + expr.length);
        // Scan all characters one by one
        for (int i = 0; i < expr.length; i++) {
            String c = expr[i];

            // If the scanned character is T or F,
            // push it to the stack.
            if (c.equals("true") || c.equals("false")) {
                //System.out.println("Adding operator " + c + " to the stack");
                stack.push(c);
            } else {
                //System.out.println("current string: " + c);
                //  If the scanned character is an operator, pop two
                // elements from stack apply the operator
                //System.out.println("Stack count before pop: " + stack.size());
                if(stack.size()<2){
                    return "null";
                }
                String val1 = stack.pop();
                String val2 = stack.pop();

                switch (c) {
                    // and case
                    case "&&": {
                         //System.out.println(val1 + "&&" + val2);
                        if (val1.equals("true") && val2.equals("false")) {
                            // T && F
                            stack.push("false");
                        } else if (val1.equals("false") && val2.equals("true")) {
                            // F && T
                            stack.push("false");
                        } else if (val1.equals("false") && val2.equals("false")) {
                            // F && F
                            stack.push("false");
                        } else {
                            // True && True
                            stack.push("true");
                        }
                        break;
                    }

                    // or case
                    case "||": {
                        //System.out.println(val1 + "||" + val2);
                        if (val1.equals("true") && val2.equals("false")) {
                            // T || F
                            stack.push("true");
                        } else if (val1.equals("false") && val2.equals("true")) {
                            // F || T
                            stack.push("true");
                        } else if (val1.equals("false") && val2.equals("false")) {
                            // F || F
                            stack.push("false");
                        } else {
                            // True || True
                            stack.push("true");
                        }
                        break;
                    }
                }
            }
        }
        if(stack.isEmpty()){
            return "null";
        }
        return stack.pop();
    }
}
