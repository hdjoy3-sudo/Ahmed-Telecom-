// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}

tasks.register("inspectJs") {
  doLast {
    try {
      val text = file("remote_js.txt").readText()
      val output = StringBuilder()
      
      // Look for Bengali string literals in the JS file
      val bStringRegex = "\"[^\"]*[\\u0980-\\u09FF]+[^\"]*\"|'[^']*[\\u0980-\\u09FF]+[^']*'".toRegex()
      val bStrings = bStringRegex.findAll(text).map { it.value }.distinct().toList()
      output.append("ALL BENGALI STRINGS FOUND (${bStrings.size}):\n")
      bStrings.forEach { output.append(" - $it\n") }
      
      // Look for Firebase/Database collections or fields
      output.append("\nFIREBASE/DB REFERENCES:\n")
      val collectionRegex = "collection\\([^,]+,\\s*\"([^\"]+)\"\\)".toRegex()
      collectionRegex.findAll(text).map { it.groupValues[1] }.distinct().forEach { output.append(" - Collection: $it\n") }
      
      // Print snippets around mentions of "owner", "admin", "view", "role", "sales", "mrp", "dp"
      output.append("\nCODE LOGIC HIGHLIGHTS:\n")
      val keywords = listOf("onAddSale", "sales", "profit", "stock", "dpAtSale", "bazar", "kpi-card", "json", "whatsapp", "backup", "restore", "owner", "admin", "view", "role", "model", "variant", "dp", "mrp", "memo")
      keywords.forEach { kw ->
        output.append("=== '$kw' ===\n")
        var lastIdx = -1
        var count = 0
        while (count < 5) {
          val idx = text.indexOf(kw, lastIdx + 1, ignoreCase = true)
          if (idx == -1) break
          val snippet = text.substring(maxOf(0, idx - 150), minOf(text.length, idx + 150))
          output.append("[$count] ...$snippet...\n\n")
          lastIdx = idx
          count++
        }
      }
      
      file("extracted_strings.txt").writeText(output.toString())
      println("Successfully wrote ${output.length} characters to extracted_strings.txt")
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}




