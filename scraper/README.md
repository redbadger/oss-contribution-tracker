# github-scraper

A Clojure library designed to scrape open source contributions
made by a Github organisation members

## Usage

At the moment, this works as a CLI app, just run it with

`lein run`

## Installation

This is a little tricky because we use Datomic. To set up a local development
environment, you'll need to get Datomic first. That process is a little involved:

1. Copy `.example` files and fill in the missing values.

1. Create a file called `credentials.clj` in `~/.lein/` with the following content:

   ```
   {#"my\.datomic\.com" {:username "greg.stewart@red-badger.com"
                      :password "[the password here]"}}
   ```

   You can get the password from Greg Stewart, Viktor Charypar or Joe Paice.

1. Get `gpg` installed

   `$ brew install gnupg`

1. Create a keypair

   `$ gpg --gen-key`

1. Encrypt the `credentials.clj` file with `gpg`

   `$gpg --default-recipient-self -e ~/.lein/credentials.clj > ~/.lein/credentials.clj.gpg`

   Try decrypting with

   `gpg --quiet --batch --decrypt ~/.lein/credentials.clj.gpg`

   you will probably get an error saying `can't query passphrase in batch mode`.
   To work around that

1. Install and run `gpg-agent`

   `$ brew install gpg-agent`

   adn enable it in `~/.gnupg/gpg.conf` by uncommenting the line saying `use-agent`.

1. In the same terminal window you will eventually run `lein run` or `lein test`
   start the agent:

   `$ gpg-agent --daemon`

   Copy the output and paste it and run it. It will set the agent info in your
   environment

   Now run the decryption again

   `gpg --quiet --decrypt ~/.lein/credentials.clj.gpg`

   This should give you a big password prompt, which will remember the password for
   a while.

1. Now you can run tests

   `$ lein test`

   To install the dependencies and run tests. You will not need to repeat this process
   once you installed datomic once.

1. To be able to run with persistence, you need to download the local datomic transactor
   from mydatomic (get credentials from Greg Stewart) into a directory called `datomic`
   and unzip it. Then you can run it by running the `run-datomic.sh` shell script in the
   root of the repo

   `$ ./run-datomic.sh`

1. Finally, create the database and initialise the schema by running

   `$ lein run -m schema.0-initial`

1. This should let you run `lein run` and start scraping!



See [the leiningen authentication guide](https://github.com/technomancy/leiningen/blob/master/doc/DEPLOY.md#authentication)
and [the PGP guide](https://github.com/technomancy/leiningen/blob/master/doc/GPG.md)
for extra context.

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
