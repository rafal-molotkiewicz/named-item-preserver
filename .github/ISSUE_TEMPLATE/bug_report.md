<!--
  Bug report template for Named Item Preserver
  Please keep reports focused, reproducible, and include environment details.
-->

**Short description**
A one-line summary of the bug.

**Steps to reproduce**
1. Step one
2. Step two
3. ...

**Expected result**
What you expected to happen.

**Actual result**
What actually happened (include exact console/server log lines if present).

**Environment**
- Minecraft version: 
- Named Item Preserver version: 
- Other mods (list all): 

IMPORTANT: If the list of other mods includes any that can affect gameplay (for example: mods that change item/mob behaviour, despawn logic, optimizers, or mixin-heavy coremods), please *remove those mods and re-test before filing*. If the issue only occurs when such mods are present, do not file a bug here — instead re-test without them and only report if the problem still reproduces.

**Relevant logs / crash reports**
Paste or attach server/client logs. If the console shows a named-item-preserver log line, please copy and paste the exact log line(s) verbatim — do not paraphrase, summarize, or retype from memory.

Exact text (including any typos) is important for diagnosis because small differences or merged/misremembered lines can point to a different mod or code path. If you need to redact sensitive values, replace them with `[REDACTED]` but keep the rest of the line unchanged.

**Reproduction commands / data**
Provide commands, NBT data, or steps to recreate the item/entity (example: `/summon item ...`, `data merge entity ...`).

Quick commands that help debug item despawn/age:

```mcfunction
/data get entity @e[type=item,sort=nearest,limit=1] Age
/data merge entity @e[type=item,sort=nearest,limit=1] {Age:5999s}
```

**Screenshots / recordings**
(Optional) links or attachments.

Thank you — providing these details makes it much easier to triage and reproduce the bug.
