/*
 * Copyright 2010-2024 Australian Signals Directorate
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
/**
 * A new HTML help user interface for Constellation.
 * <p>
 * This package provides a browser-based help system that allows existing
 * {@code HelpCtx()} based code to work unchanged.
 * <h2>Help format</h2>
 *
 * <p>
 * Help is provided as a map of markdown and other resources that can be
 * displayed by a standard web browser, located in each modules docs and 
 * resources directories. The following assumptions are made about each
 * package wanting to implement the help pages:</p>
 * <ul>
 * <li>Class that extends HelpPageProvider</li>
 * <li>XML file with mappings to class names of callers</li>
 * <li>Relevant help pages in markdown to display</li>
 * </ul>
 * 
 * <p>
 * To enable the mapping of helpId (provided by {@code HelpCtx()} to the page,
 * a mapping file is also required. This file is called {@code help-toc.xml} and
 * is located within the docs package of the module.</p>
 *
 * <p>
 * For example, a help toc might like this, where
 * {@code help-options.md},  (@code help-toc.xml} and (@code CoreHelpHelpProvider.java} 
 * are the only required files.</p>
 *
 * <pre>
 * help-toc.xml
 * <?xml version="1.0" encoding="UTF-8"?>
 * <!DOCTYPE toc PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN" "http://java.sun.com/products/javahelp/toc_2_0.dtd">
 * <toc version="2.0">
 *     <tocitem text="Preferences">
 *         <tocitem text="Online Help" target="au.gov.asd.tac.constellation.help.preferences.HelpOptionsPanelController"/>
 *     </tocitem>
 * </toc>
 * </pre>
 *
 * <h2>help-toc.xml format</h2>
 *
 * <p>
 * The {@code help-toc.xml} is a simple XML file. The correct setup tags are
 * necessary and are listed as the first two lines of the file. All content
 * after the first two lines must be enclosed within a <toc version="2.0"></toc>
 * tag. The next item can be content or headings. A heading is defined by a
 * <tocitem text="Heading"></tocitem> tag. Where "Heading" will be the heading.
 * The content items will also be tocitem tagged as follows.
 * <tocitem text="Online Help" target="au.gov.asd.tac.constellation.help.preferences.HelpOptionsPanelController"/>
 * The differentiating feature of this is the target attribute. The value of the
 * attribute is a helpId as used by a {@code HelpCtx()} instance. This will
 * assist the help module in providing the relevant links to your files.
 *
 * <p>
 * There are two options for presenting the help.</p>
 *
 * <h3>Offline</h3>
 * <p>
 * This is the default method. The files are packaged with Constellation and as
 * long as the host system is able to use a browser, the help documentation will
 * launch from that window.
 * </p>
 *
 * <h3>Online</h3>
 * <p>
 * Online help will simply direct the browser to the URL of the specified help
 * contents available on the Constellation-app website.
 * </p>
 *
 * <h3>Help Page Preferences</h3>
 *
 * <p>
 * A Preference is available in the Constellation Preferences menu to toggle
 * between viewing online and offline help pages.</p>
 *
 *
 *
 * <p>
 * Constellation's internal web server is used to present the locally converted
 * files to the user's browser. (See details below.)
 *
 * <h3>External web server.</h3>
 *
 * <p>
 * The help files are served from an external web server. How the files are
 * stored is up to the web server, as long as they are available in the same
 * layout as above.
 * </p>
 *
 * <p>
 * Using this method means that all help is served to the user's browser from
 * the specified web server. After the initial browse to a URL after the user's
 * help request, Constellation is not otherwise involved.</p>
 *
 * <h2>How it works</h2>
 *
 * <p>
 * NetBeans applications request help to be displayed for a helpId either by
 * explicitly calling {@code HelpCtx.display()}, or by associating a
 * {@code HelpCtx()} with a NetBeans help-aware object (such as a TopComponent).
 * A helpId is associated with a help page via a {@code -map.xml} file in a
 * helpset. The rest of the helpset information allows NetBeans to combine all
 * the helpsets into a single JavaHelp display.</p>
 *
 * <p>
 * The {@code ConstellationHelp} class overrides the built-in NetBeans help
 * provider and looks up the highest priority {@code HelpCtx.Displayer} class,
 * which happens to be {@code ConstellationHelpDisplayer}. Depending on the
 * online preference value, the {@code ConstellationHelpDisplayer} converts the
 * .md files into html on request. This allows for dynamic page loads depending
 * on what content needs to be served.</p>
 *
 * <p>
 * The URL uses the internal web server on * localhost. A help servlet with endpoint {@code /help} accepts the request and
 * calls {@code ConstellationHelpDisplayer.copy()} to copy the resource from the
 * zip
 * file to the servlet's output stream. During this process is when any files
 * which need to be converted from .md to .html are converted.</p>
 *
 */
package au.gov.asd.tac.constellation.help;
