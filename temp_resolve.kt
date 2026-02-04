fun DUIIcon.resolve(): ImageVector {
    val name = this.name.lowercase()
    return when {
        name.endsWith("_outlined") -> resolveOutlinedIcon(name.removeSuffix("_outlined"))
        name.endsWith("_rounded") -> resolveRoundedIcon(name.removeSuffix("_rounded"))
        name.endsWith("_sharp") -> resolveSharpIcon(name.removeSuffix("_sharp"))
        else -> resolveFilledIcon(name)
    }
}
