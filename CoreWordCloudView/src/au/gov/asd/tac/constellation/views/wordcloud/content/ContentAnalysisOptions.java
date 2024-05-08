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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author twilight_sparkle
 */
public class ContentAnalysisOptions {
    
    private ContentAnalysisOptions() {
        throw new IllegalStateException("Utility class");
    }

    public enum Delimiter {

        SPACE {
            @Override
            public String toString() {
                return "space";
            }

            @Override
            public char getChar() {
                return ' ';
            }
        },
        COMMA {
            @Override
            public String toString() {
                return ", (comma)";
            }

            @Override
            public char getChar() {
                return ',';
            }
        },
        BACKSLASH {
            @Override
            public String toString() {
                return "\\ (backslash)";
            }

            @Override
            public char getChar() {
                return '\\';
            }
        },
        SLASH {
            @Override
            public String toString() {
                return "/ (slash)";
            }

            @Override
            public char getChar() {
                return '/';
            }
        },
        FULL_STOP {
            @Override
            public String toString() {
                return ". (full stop)";
            }

            @Override
            public char getChar() {
                return '.';
            }
        };

        public abstract char getChar();

        public static Delimiter defaultEnum() {
            return SPACE;
        }

        public static Delimiter getValue(final String value) {
            if (value.equalsIgnoreCase(SPACE.toString())) {
                return SPACE;
            } else if (value.equalsIgnoreCase(COMMA.toString())) {
                return COMMA;
            } else if (value.equalsIgnoreCase(BACKSLASH.toString())) {
                return BACKSLASH;
            } else if (value.equalsIgnoreCase(SLASH.toString())) {
                return SLASH;
            } else if (value.equalsIgnoreCase(FULL_STOP.toString())) {
                return FULL_STOP;
            } else {
                return SPACE;
            }
        }

        public static List<String> getChoices() {
            final List<String> list = new ArrayList<>();
            list.add(SPACE.toString());
            list.add(COMMA.toString());
            list.add(BACKSLASH.toString());
            list.add(SLASH.toString());
            list.add(FULL_STOP.toString());
            return list;
        }
    }

    public enum TokenThresholdMethod {
        APPEARANCE {
            @Override
            public String toString() {
                return "Proportion of elements token appeared in";
            }
        },
        RANK {
            @Override
            public String toString() {
                return "Percentile of tokens ranked by appearances";
            }
        };

        public static TokenThresholdMethod defaultEnum() {
            return APPEARANCE;
        }

        public static TokenThresholdMethod getValue(final String value) {
            if (value.equalsIgnoreCase(APPEARANCE.toString())) {
                return APPEARANCE;
            } else if (value.equalsIgnoreCase(RANK.toString())) {
                return RANK;
            } else {
                return APPEARANCE;
            }
        }

        public static List<String> getChoices() {
            final List<String> list = new ArrayList<>();
            list.add(APPEARANCE.toString());
            list.add(RANK.toString());
            return list;
        }
    }

    public enum TokenizingMethod {

        NGRAMS {
            @Override
            public String toString() {
                return "n-grams";
            }
        },
        DELIMITED_NGRAMS {
            @Override
            public String toString() {
                return "delimited n-grams";
            }
        },
        NWORDS {
            @Override
            public String toString() {
                return "n-words";
            }
        };

        public static TokenizingMethod defaultEnum() {
            return NGRAMS;
        }

        public static TokenizingMethod getValue(final String value) {
            if (value.equalsIgnoreCase(NGRAMS.toString())) {
                return NGRAMS;
            } else if (value.equalsIgnoreCase(DELIMITED_NGRAMS.toString())) {
                return DELIMITED_NGRAMS;
            } else if (value.equalsIgnoreCase(NWORDS.toString())) {
                return NWORDS;
            } else {
                return NGRAMS;
            }
        }

        public static List<String> getChoices() {
            final List<String> list = new ArrayList<>();
            list.add(NGRAMS.toString());
            list.add(DELIMITED_NGRAMS.toString());
            list.add(NWORDS.toString());
            return list;
        }
    }

    /**
     * An enumeration of options for the follow up action to be performed upon
     * executing the n-grams analysis plugin
     */
    public enum FollowUpChoice {

        CLUSTER {
            @Override
            public String toString() {
                return "Cluster";
            }
        },
        ADD_TRANSACTIONS {
            @Override
            public String toString() {
                return "Add Transactions";
            }
        },
        MAKE_SELECTIONS {
            @Override
            public String toString() {
                return "Make Selections";
            }
        };

        public static FollowUpChoice defaultEnum() {
            return CLUSTER;
        }

        public static FollowUpChoice getValue(final String value) {
            if (value.equalsIgnoreCase(CLUSTER.toString())) {
                return CLUSTER;
            } else if (value.equalsIgnoreCase(MAKE_SELECTIONS.toString())) {
                return MAKE_SELECTIONS;
            } else {
                return ADD_TRANSACTIONS;
            }
        }

        public static List<String> getDocumentClusteringChoices() {
            final List<String> list = new ArrayList<>();
            list.add(CLUSTER.toString());
            list.add(MAKE_SELECTIONS.toString());
            return list;
        }

        public static List<String> getNodeSimilarityChoices() {
            final List<String> list = new ArrayList<>();
            list.add(ADD_TRANSACTIONS.toString());
            return list;
        }
    }

    public enum FollowUpScope {

        ALL {
            @Override
            public String toString() {
                return "All";
            }
        },
        SELECTED {
            @Override
            public String toString() {
                return "Selected";
            }
        },
        SIMILAR_TO_SELECTED {
            @Override
            public String toString() {
                return "Similar to Selected";
            }
        };

        public static FollowUpScope defaultEnum() {
            return ALL;
        }

        public static FollowUpScope getValue(final String value) {
            if (value.equalsIgnoreCase(ALL.toString())) {
                return ALL;
            } else if (value.equalsIgnoreCase(SELECTED.toString())) {
                return SELECTED;
            } else if (value.equalsIgnoreCase(SIMILAR_TO_SELECTED.toString())) {
                return SIMILAR_TO_SELECTED;
            } else {
                return ALL;
            }
        }

        public static List<String> getChoices() {
            final List<String> list = new ArrayList<>();
            list.add(ALL.toString());
            list.add(SELECTED.toString());
            list.add(SIMILAR_TO_SELECTED.toString());
            return list;
        }
    }
}
