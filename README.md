Neurofiction
============

[neurofiction.net](http://neurofiction.net) is a genre of stories that change themselves in response to the reader's brain activity.

This is the open-source project behind the first proof of concept story by Hannu Rajaniemi: "Snow White is Dead".

The software is written in Scala by Sam Halliday and requires an [Emotiv EPOC headset](https://github.com/fommil/emokit-java) (approximately [$300](http://www.emotiv.com/store/hardware/epoc-bci/epoc-neuroheadset/)).

To keep up-to-date, follow [`#neurofic`](https://twitter.com/search?q=neurofic).

Installing / Running
====================

This software has been optimised to run on an iMac monitor screen and may render in a strange manner for other devices. It has been designed as an art installation and is not particularly user-friendly for individuals. However, with some knowledge of UNIX systems, it is possible to reproduce the experience by following these instructions.

* A [PostgreSQL database](http://www.postgresql.org) must be available on `localhost:5432/outsight`. Edit `persistence.xml` to point somewhere else, or to use a different relational database.
* Clone this repository
* type `sbt run`.

Pressing `ESC` will exit, otherwise it will run in a loop.


Donations
=========

Please consider supporting the maintenance of this open source project with a donation:

[![Donate via Paypal](https://www.paypal.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=B2HW5ATB8C3QW&lc=GB&item_name=neurofiction&currency_code=GBP&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)


Licence
=======

Copyright (C) 2012 Samuel Halliday  
Copyright (C) 2012 Hannu Rajaniemi

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see http://www.gnu.org/licenses/


Contributing
============

Contributors are encouraged to fork this repository and issue pull
requests. Contributors implicitly agree to assign an unrestricted licence
to Sam Halliday, but retain the copyright of their code (this means
we both have the freedom to update the licence for those contributions).
