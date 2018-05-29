CREATE TABLE "file" (
  "id" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
  "type" integer NOT NULL,
  "path" text NOT NULL DEFAULT '',
  "file_name" text NOT NULL DEFAULT '',
  "mtime" integer NOT NULL,
  "extra_info" text NOT NULL DEFAULT '',
  "update_time" text NOT NULL
);

CREATE UNIQUE INDEX "file_uniq_info"
ON "file" (
  "path",
  "file_name"
);

CREATE TABLE "sync_path" (
  "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  "path" TEXT NOT NULL DEFAULT ''
);

CREATE UNIQUE INDEX "sync_path_uniq_info"
ON "sync_path" (
  "path"
);

CREATE TABLE "sync_status" (
  "key" TEXT NOT NULL,
  "value" TEXT NOT NULL
);

CREATE UNIQUE INDEX "sync_status_uniq_info"
ON "sync_status" (
  "key",
  "value"
);

INSERT INTO sync_status("key", "value") VALUES ("flag", "INIT");
