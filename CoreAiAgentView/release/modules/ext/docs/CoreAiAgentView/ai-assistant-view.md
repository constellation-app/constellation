# Assistant View

The Assistant is a chat panel that can inspect and act on the active graph. It talks to a large language model through the Anthropic API or an OpenAI-compatible API (OpenAI, Azure OpenAI, vLLM, LM Studio, LiteLLM and others). The assistant works in steps: it can call *tools* that read the graph, run plugins and Data Access View plugins, select elements and write notes, and then use the results to answer you.

Open it from *Views > Assistant* or press <kbd>Ctrl+Shift+K</kbd>.

## Setting up a model

The Assistant doesn't come with a model. Until you configure one, the view shows **Configure a model…** and messages can't be sent. To set one up:

1. Press **Configure a model…** (or open *Tools > Options > Constellation > Assistant*).
2. On the **Endpoints** tab press **Add**, and choose the **Kind**: *Anthropic* or *OpenAI-compatible*.
3. Enter the **Base URL**, for example `https://api.anthropic.com/v1`, `https://api.openai.com/v1` or `http://localhost:4000/v1` (LiteLLM), and the **Key** (API key) if the endpoint needs one.
4. Press **Test connection** to check the connection and list the endpoint's models, enter one of them as the **Model**, and press **OK**.

## Choosing a model

Use the drop-downs at the top of the view to pick an **endpoint** and a **model**. The model list is read from the endpoint; you can also type a model id. Press ⟳ to refresh the list. If the model list can't be read (for example because the endpoint isn't running or has no API key), a warning is shown under the drop-downs.

Press ⚙ for per-chat settings: **temperature**, **reasoning effort** (choose *none* to turn reasoning off on models that support it, which makes small models much faster) and the **maximum steps** the assistant can take per message.

Small models are less reliable at multi-step tool use than large hosted models. Setting **Reasoning** to *none* makes them much faster, but they then make more mistakes; if the assistant stops part-way, tell it to continue.

## Chatting

Type a question and press <kbd>Enter</kbd> (<kbd>Shift+Enter</kbd> adds a new line). Each message is sent with a short, automatically generated summary of the active graph (its name, schema, size and selection), so the assistant knows what you are looking at.

Answers stream in as they are written. Tool calls appear as cards in the transcript showing the tool, its arguments and a summary of the result; expand *arguments* or *result* to see the details.

Press **■ Stop** at any time to stop the assistant, whether it is waiting for the model, waiting for your approval, or running a plugin. Press **+ New chat** to start again.

## Approving tool calls

Tools that only read the graph run automatically. Tools that change the graph or query external systems ask first; their card shows:

- **Approve** (or <kbd>Enter</kbd>) – run the call.
- **Always allow** – run it, and run this tool without asking for the rest of the chat.
- **Edit JSON** – change the arguments before running.
- **Deny…** (or <kbd>Esc</kbd>) – don't run it. You can give a reason, which is passed to the assistant.

If the active graph changes while a call is waiting for approval, the card warns you; the call runs on the graph that is active when you approve it.

Every change the assistant makes runs as a normal plugin, so it can be undone with *Edit > Undo* and is recorded in the Notes View and the Plugin Reporter.

## Tools

| Tool | What it does | Default |
|---|---|---|
| `get_graph_summary`, `list_graphs`, `get_attributes` | Describe the open graphs and their attributes | Auto |
| `query_elements`, `attribute_stats`, `get_selection` | Find and summarise vertices and transactions | Auto |
| `select_elements` | Change the selection (undoable) | Auto |
| `search_plugins`, `describe_plugin` | Find plugins and their parameters | Auto |
| `run_plugin` | Run an analytic, arrangement or other plugin | Ask |
| `list_data_access_plugins`, `run_data_access_plugin` | Find and run Data Access View plugins | Auto / Ask |
| `add_records` | Add vertices and transactions | Ask |
| `add_note` | Write a note tagged "AI" to the Notes View | Auto |
| `set_active_graph`, `undo_last_change` | Switch graphs, undo the last change | Ask |

Change these policies under *Tools > Options > Constellation > Assistant > Tools*: **AUTO** runs without asking, **ASK** asks each time and **NEVER** hides the tool from the assistant.

## Options

*Tools > Options > Constellation > Assistant* (or *⋯ > Options…* in the view):

- **Endpoints** – add, edit and remove endpoints. For each endpoint set the kind (*Anthropic* or *OpenAI-compatible*), the base URL, the model and any extra HTTP headers (one `Name: value` per line, sent with every request, e.g. for gateways). **Test connection** lists the endpoint's models and shows which client certificate is in use.
- **API keys** are saved with the other Assistant settings in Constellation's preferences, so you only enter each key once. If no key is saved for an Anthropic endpoint, the `ANTHROPIC_API_KEY` environment variable is used.
- **Client certificates and proxies** – there is nothing to set up. If Constellation is configured with a client certificate, it is presented to any endpoint that asks for one (mTLS); if not, endpoints are contacted without one. Requests always use Constellation's proxy settings.
- **System Prompt** – the instructions sent to the model at the start of each chat.

## Privacy and safety

- Your messages, a summary of the active graph and the results of the tools the assistant uses (which can include attribute values) are sent to the selected endpoint.
- Password parameters are never shown to or accepted from the assistant, and secret-looking arguments are masked on tool cards.
- Graph contents are treated as data: the assistant is instructed not to follow instructions found in the graph, and anything that changes the graph needs your approval by default.
- Use *⋯ > Copy transcript* or *⋯ > Export transcript…* to keep a record of a conversation as Markdown or JSON.
