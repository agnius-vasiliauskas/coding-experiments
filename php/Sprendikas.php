<?php
/**
 * Created by PhpStorm.
 * User: agnius
 * Date: 17.8.5
 * Time: 16.40
 */

class Sprendikas {

    public $atsakymas;

    function __construct($lygtis)
    {
        $cmd = "maxima --very-quiet -r \"ratprint: false$ numer:true$ solve ([{$lygtis}], [x]);\"";
        $out = shell_exec($cmd);
        preg_match_all('/\[\w\s+=\s+(\d+(?:\.\d+)?)\]/mui', $out, $ats, PREG_SET_ORDER, 0);
        $this->atsakymas = $ats[0][1];
    }
}
