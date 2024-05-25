"""Export environment variables to a .bzl file in an external repo"""

def _impl_create_vars(rctx):
    """Create a .bzl file in an external repo with the given vars"""
    vars = rctx.attr.vars
    rctx.file(
        "vars.bzl",
        content = "\n".join([
            "{key} = \"{value}\"".format(key = key, value = value)
            for key, value in vars.items()
        ]),
    )
    rctx.file(
        "BUILD",
        content = "exports_files(['vars.bzl'])",
    )

create_vars = repository_rule(
    implementation = _impl_create_vars,
    attrs = {
        "vars": attr.string_dict(mandatory = True),
    },
    doc = "Create a .bzl file in an external repo with the given vars",
)

def _impl_constants(mctx):
    vars = {}
    for mod in mctx.modules:
        for tag in mod.tags.export:
            if tag.name not in vars:
                vars[tag.name] = {}
            vars[tag.name][tag.key] = tag.value

    for repo_name, vars in vars.items():
        create_vars(
            name = repo_name,
            vars = vars,
        )

_export = tag_class(
    doc = "Export an environment variable to a .bzl file in an external repo",
    attrs = {
        "name": attr.string(mandatory = True),
        "key": attr.string(mandatory = True),
        "value": attr.string(mandatory = True),
    },
)

constants = module_extension(
    implementation = _impl_constants,
    tag_classes = {
        "export": _export,
    },
)
